package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.{FiberIO, IO, Ref}
import cats.syntax.all.*
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.{GameConfig, GameEngine}
import com.unibo.scalaparty.infrastructure.model.{ActiveMatch, JoinOutcome, PlayerId, ServerMessage}
import com.unibo.scalaparty.infrastructure.network.ConnectionRegistry
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, MatchEventPublisher, PlayerNotifier}
import com.unibo.scalaparty.core.model.GameSettings

/** Application service driving the whole life of a match, from the waiting queue to the last tick.
 *
 *  It is the piece that closes the loop: [[QueuedLobbyManager]] only decides *who* plays next, and
 *  [[MatchRunner]] only knows how to tick a match that already exists. This coordinator picks the
 *  players, spawns the runner on its own fiber, waits for it to finish and then hands the arena to
 *  whoever is next in the queue, so the queue keeps moving on its own.
 *
 *  Only one match runs at a time: starting a new one cancels whatever was still running, which
 *  matters when a match is cut short because everybody left it.
 *
 *  @param lobby         Decides who plays and who waits.
 *  @param registry      Tracks which connection belongs to which match.
 *  @param commands      Buffer the gameplay inputs are drained from.
 *  @param notifier      Tells a single player what is happening to it.
 *  @param publisher     Broadcasts the authoritative state to everybody in the match.
 *  @param settings      Shared rules and arena dimensions for the engine.
 *  @param matchDuration How long a match lasts, there being no win condition yet.
 *  @param running       The fiber ticking the current match, if any.
 */
class MatchCoordinator(
    lobby: QueuedLobbyManager[IO],
    registry: ConnectionRegistry,
    commands: GameCommandService,
    notifier: PlayerNotifier[IO],
    publisher: MatchEventPublisher[IO],
    settings: GameSettings,
    matchDuration: FiniteDuration,
    running: Ref[IO, Option[FiberIO[Unit]]]
) extends AccessPort[IO]:

  /** Takes a player in, joining the lobby and either starting a match immediately
   * or queueing the player with a notification of those ahead.
   *
   * @param playerId the unique identifier of the joining player
   * @return an effect completing when the lobby action is handled
   */
  override def joinLobby(playerId: PlayerId): IO[Unit] =
    lobby.join(playerId).flatMap:
      case JoinOutcome.Playing(activeMatch) => startMatch(activeMatch)
      case JoinOutcome.Queued(playersAhead) => notifier.send(playerId, ServerMessage.Queued(playersAhead))

  /** Removes a player from the lobby or active match, cancelling running matches if emptied
   * and refreshing queues or starting successors as appropriate.
   *
   * @param playerId the unique identifier of the leaving player
   * @return an effect completing when the leave action is processed
   */
  override def leaveLobby(playerId: PlayerId): IO[Unit] =
    for
      started <- lobby.leave(playerId)
      current <- lobby.currentMatch
      // The match may have died with nobody to take over: its runner would otherwise keep ticking
      // for an arena no one is in. When a successor exists, starting it cancels the old one anyway.
      _ <- IO.whenA(current.isEmpty)(cancelRunning)
      _ <- refreshQueue
      _ <- started.traverse_(startMatch)
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
      _     <- cancelRunning
      _     <- admit(activeMatch)
      fiber <- play(activeMatch).start
      _     <- running.set(Some(fiber))
    yield ()

  /** Assigns each player in the match to the connection registry and notifies them that the match has started.
   *
   * @param activeMatch the active match being populated
   * @return an effect completing when all players are admitted
   */
  private def admit(activeMatch: ActiveMatch): IO[Unit] =
    activeMatch.players.toList.traverse_ { playerId =>
      registry.assignToMatch(playerId, activeMatch.matchId) *>
        notifier.send(playerId, ServerMessage.MatchStarted(activeMatch.players.size))
    }

  /** Runs the match to completion, then hands the arena over to the next group of players.
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
      // Forget the fiber first: this code runs inside it, and starting the next match cancels
      // whatever is recorded here, which would otherwise cancel us halfway through.
      _ <- running.set(None)
      _ <- activeMatch.players.toList.traverse_ { playerId =>
        registry.clearMatch(playerId) *> notifier.send(playerId, ServerMessage.MatchEnded)
      }
      next <- lobby.finishMatch(activeMatch.matchId)
      _    <- refreshQueue
      _    <- next.traverse_(startMatch)
    yield ()

  /** Cancels the currently running match fiber, if any.
   *
   * @return an effect completing when the running fiber is cancelled
   */
  private def cancelRunning: IO[Unit] =
    running.getAndSet(None).flatMap(_.traverse_(_.cancel))

object MatchCoordinator:

  /** Factory method that safely initializes the MatchCoordinator with a reference to track the running fiber.
   *
   * @param lobby         the queued lobby manager deciding who plays
   * @param registry      the connection registry tracking player sockets
   * @param commands      the service buffering player inputs
   * @param notifier      the port delivering personal messages to players
   * @param publisher     the publisher broadcasting match states
   * @param settings      Shared rules and arena dimensions for the engine
   * @param matchDuration the duration of each match session
   * @return an IO effect containing the instantiated MatchCoordinator
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
      .of[IO, Option[FiberIO[Unit]]](None)
      .map(new MatchCoordinator(lobby, registry, commands, notifier, publisher, settings, matchDuration, _))
