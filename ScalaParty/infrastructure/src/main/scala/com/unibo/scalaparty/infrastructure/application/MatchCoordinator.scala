package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.{Deferred, FiberIO, IO, Ref}
import cats.syntax.all.*
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.{GameConfig, GameEngine}
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.infrastructure.model.{ActiveMatch, Admission, JoinOutcome, MatchId, PlayerId, ServerMessage}
import com.unibo.scalaparty.infrastructure.network.ConnectionRegistry
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, MatchEventPublisher, PlayerNotifier}

/** Application service driving the whole life of a match, from the waiting queue to the last tick.
 *
 *  It is the piece that closes the loop: [[QueuedLobbyManager]] only decides *who* plays next, and
 *  [[MatchRunner]] only knows how to tick a match that already exists. This coordinator picks the
 *  players, spawns the runner on its own fiber, waits for it to finish and then hands the room to
 *  whoever is next in the queue, so the queue keeps moving on its own.
 *
 *  Several matches run side by side, each on its own fiber and independent of the others: a match
 *  is stopped only when it ends or when everybody has left it.
 *
 *  @param lobby         Decides who plays and who waits.
 *  @param registry      Tracks which connection belongs to which match.
 *  @param commands      Buffer the gameplay inputs are drained from.
 *  @param notifier      Tells a single player what is happening to it.
 *  @param publisher     Broadcasts the authoritative state to everybody in the match.
 *  @param settings      Shared rules and arena dimensions for the engine.
 *  @param matchDuration How long a match lasts, there being no win condition yet.
 *  @param running       The fiber ticking each match being played.
 */
class MatchCoordinator(
    lobby: QueuedLobbyManager[IO],
    registry: ConnectionRegistry,
    commands: GameCommandService,
    notifier: PlayerNotifier[IO],
    publisher: MatchEventPublisher[IO],
    settings: GameSettings,
    matchDuration: FiniteDuration,
    running: Ref[IO, Map[MatchId, FiberIO[Unit]]]
) extends AccessPort[IO]:

  /** Takes a player in, joining the lobby and either starting a match immediately
   *  or queueing the player with a notification of those ahead. A player finding the queue full is
   *  told so and turned away.
   *
   *  @param playerId the unique identifier of the joining player
   *  @return an effect reporting whether the player was taken in
   */
  override def joinLobby(playerId: PlayerId): IO[Admission] =
    lobby.join(playerId).flatMap:
      case JoinOutcome.Playing(activeMatch) =>
        startMatch(activeMatch).as(Admission.Admitted)
      case JoinOutcome.Queued(playersAhead) =>
        notifier.send(playerId, ServerMessage.Queued(playersAhead)).as(Admission.Admitted)
      case JoinOutcome.Rejected =>
        notifier.send(playerId, ServerMessage.QueueFull).as(Admission.Rejected)

  /** Removes a player from the lobby or from its match, stopping that match if it was emptied
   *  and refreshing queues or starting successors as appropriate.
   *
   *  @param playerId the unique identifier of the leaving player
   *  @return an effect completing when the leave action is processed
   */
  override def leaveLobby(playerId: PlayerId): IO[Unit] =
    for
      outcome <- lobby.leave(playerId)
      // The match may have died with its last player: its runner would otherwise keep ticking for a
      // room no one is in.
      _ <- outcome.disbanded.traverse_(stop)
      _ <- refreshQueue
      _ <- outcome.started.traverse_(startMatch)
    yield ()

  /** Tells every waiting player how many others are still ahead of it. */
  def refreshQueue: IO[Unit] =
    lobby.waitingPlayers.flatMap: waiting =>
      waiting.zipWithIndex.traverse_((playerId, position) => notifier.send(playerId, ServerMessage.Queued(position)))

  /** Binds the chosen players to the match and starts ticking it on a dedicated fiber.
   *
   *  @param activeMatch the match instance containing the assigned players
   *  @return an effect completing when the match fiber is spawned
   */
  private def startMatch(activeMatch: ActiveMatch): IO[Unit] =
    for
      _          <- admit(activeMatch)
      registered <- Deferred[IO, Unit]
      // The match waits for its fiber to be recorded: were it to end first, its cleanup would find
      // nothing to forget and the finished fiber would be recorded afterwards, never to be removed.
      fiber <- (registered.get *> play(activeMatch)).start
      _     <- running.update(_ + (activeMatch.matchId -> fiber))
      _     <- registered.complete(())
    yield ()

  /** Assigns each player in the match to the connection registry and notifies them that the match has started.
   *
   *  @param activeMatch the active match being populated
   *  @return an effect completing when all players are admitted
   */
  private def admit(activeMatch: ActiveMatch): IO[Unit] =
    activeMatch.players.toList.traverse_ { playerId =>
      registry.assignToMatch(playerId, activeMatch.matchId) *>
        notifier.send(playerId, ServerMessage.MatchStarted(activeMatch.players.size))
    }

  /** Runs the match to completion, then hands the room over to the next group of players.
   *
   *  @param activeMatch the active match to run
   *  @return an effect completing when the match finishes and cleanup concludes
   */
  private def play(activeMatch: ActiveMatch): IO[Unit] =
    val mapping = activeMatch.players.map(_ -> EntityId.generate()).toMap
    val session = MatchSession(activeMatch.matchId, mapping, GameWorld(Map.empty))
    val engine = GameEngine(
      GameConfig(
        players = mapping.values.toList,
        settings = GameSettings.default
      )
    )
    val runner = new MatchRunner(session, commands, engine, publisher, matchDuration)
    runner.run.compile.drain *> concludeMatch(activeMatch)

  /** Releases the players of a finished match and lets the next one in.
   *
   *  @param activeMatch the match that has just concluded
   *  @return an effect completing when resources are released and the next match starts
   */
  private def concludeMatch(activeMatch: ActiveMatch): IO[Unit] =
    for
      // Forget the fiber first: this code runs inside it, and a player leaving now would stop
      // whatever is recorded for this match, which would otherwise cancel us halfway through.
      _ <- running.update(_ - activeMatch.matchId)
      _ <- activeMatch.players.toList.traverse_ { playerId =>
        registry.clearMatch(playerId) *> notifier.send(playerId, ServerMessage.MatchEnded)
      }
      next <- lobby.finishMatch(activeMatch.matchId)
      _    <- refreshQueue
      _    <- next.traverse_(startMatch)
    yield ()

  /** Cancels the fiber ticking the given match, if it is still running.
   *
   *  @param matchId the match to stop
   *  @return an effect completing when the fiber is cancelled
   */
  private def stop(matchId: MatchId): IO[Unit] =
    running.modify(fibers => (fibers - matchId, fibers.get(matchId))).flatMap(_.traverse_(_.cancel))

object MatchCoordinator:

  /** Factory method that safely initializes the MatchCoordinator with a reference to track the running fibers.
   *
   *  @param lobby         the queued lobby manager deciding who plays
   *  @param registry      the connection registry tracking player sockets
   *  @param commands      the service buffering player inputs
   *  @param notifier      the port delivering personal messages to players
   *  @param publisher     the publisher broadcasting match states
   *  @param settings      Shared rules and arena dimensions for the engine
   *  @param matchDuration the duration of each match session
   *  @return an IO effect containing the instantiated MatchCoordinator
   */
  def apply(
      lobby: QueuedLobbyManager[IO],
      registry: ConnectionRegistry,
      commands: GameCommandService,
      notifier: PlayerNotifier[IO],
      publisher: MatchEventPublisher[IO],
      settings: GameSettings,
      matchDuration: FiniteDuration = MatchRunner.DefaultDuration
  ): IO[MatchCoordinator] =
    Ref
      .of[IO, Map[MatchId, FiberIO[Unit]]](Map.empty)
      .map(new MatchCoordinator(lobby, registry, commands, notifier, publisher, settings, matchDuration, _))
