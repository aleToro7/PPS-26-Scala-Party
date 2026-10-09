package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.IO
import fs2.Stream
import com.unibo.scalaparty.core.ecs.{EntityId, GameEvent, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.model.{GameCommand, MatchOutcome, MatchState}
import com.unibo.scalaparty.infrastructure.application.CommandAdapter.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.MatchEventPublisher

type PlayerEntityMapping = Map[PlayerId, EntityId]

case class MatchSession(
    matchId: MatchId,
    players: PlayerEntityMapping,
    world: GameWorld
)

/** Authoritative loop of a single match: drains the buffered inputs, advances the engine by one
 *  tick and broadcasts the resulting state, over and over.
 *
 *  A match goes on when some of its players leave, as long as anybody is left in it: at every tick
 *  the players who are no longer in the match are looked up, and the engine is told to remove the
 *  spaceship of each of them, once.
 *
 *  The match is over once the engine reports a [[GameEvent.MatchEnded]] event: the loop stops right
 *  after broadcasting the state of the tick that ended it, which is what lets the waiting queue move on.
 *
 *  @param session      The match and the entity each of its players controls.
 *  @param commandQueue Buffer the gameplay inputs are drained from.
 *  @param engine       The engine advancing the game world.
 *  @param publisher    Broadcasts the authoritative state to everybody in the match.
 *  @param roster       The players still taking part in the match.
 */
class MatchRunner(
    session: MatchSession,
    commandQueue: GameCommandService,
    engine: GameEngine,
    publisher: MatchEventPublisher[IO],
    roster: IO[Set[PlayerId]]
):

  /** Ticks the match until the engine reports its end.
   *
   *  @return an effect completing with how the match ended
   */
  def run: IO[MatchOutcome] =
    Stream
      .fixedRate[IO](MatchRunner.tickInterval)
      .zipWithIndex
      // Carries along the players who already left, whose spaceships are already gone.
      .evalMapAccumulate(Set.empty[PlayerId]):
        case (departed, (_, tick)) =>
          for
            // Drain the raw commands from the infrastructure queue
            rawCommands <- commandQueue.drainCommands(session.matchId)
            present     <- roster

            // Purely resolve network intents into ECS domain commands using the MatchSession mapping
            ecsCommands = rawCommands.flatMap: (playerId, intent) =>
              session.players.get(playerId).flatMap(intent.toDto)

            // The players who left since the last tick
            leaving = session.players.keySet -- present -- departed
            leaveCommands = leaving.toList.flatMap(session.players.get).map(GameCommand.LeaveCommand(_))

            // Process the resolved commands in the game engine
            result = engine.update(ecsCommands ++ leaveCommands, MatchRunner.tickInterval.toMillis)

            // Publish the new authoritative state
            _ <- publisher.broadcastState(session.matchId, MatchState(tick, engine.arena, result.entities))

            // How the match ended, if it did during this tick
            ended = result.events.collectFirst { case GameEvent.MatchEnded(outcome) => outcome }
          yield (departed ++ leaving, ended)
      .collectFirst:
        case (_, Some(outcome)) => outcome
      .compile
      .lastOrError

object MatchRunner:
  /** The server ticks at roughly 60 frames per second. */
  val tickInterval: FiniteDuration = 16.millis
