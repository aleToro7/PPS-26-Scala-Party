package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.IO
import fs2.Stream
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.model.{GameCommand, MatchState}
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
 *  The stream is finite. There being no win condition in the game yet, a match simply lasts
 *  [[duration]] and then ends, which is what lets the waiting queue move on.
 *
 *  @param session      The match and the entity each of its players controls.
 *  @param commandQueue Buffer the gameplay inputs are drained from.
 *  @param engine       The engine advancing the game world.
 *  @param publisher    Broadcasts the authoritative state to everybody in the match.
 *  @param roster       The players still taking part in the match.
 *  @param duration     How long the match lasts.
 */
class MatchRunner(
    session: MatchSession,
    commandQueue: GameCommandService,
    engine: GameEngine,
    publisher: MatchEventPublisher[IO],
    roster: IO[Set[PlayerId]],
    duration: FiniteDuration = MatchRunner.DefaultDuration
):

  /** How many ticks fit in the match duration. */
  private val ticks: Long = (duration / MatchRunner.TickInterval).toLong

  def run: Stream[IO, Unit] =
    Stream
      .fixedRate[IO](MatchRunner.TickInterval)
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
            newEntities = engine.update(ecsCommands ++ leaveCommands, MatchRunner.TickInterval.toMillis)

            // Publish the new authoritative state
            _ <- publisher.broadcastState(session.matchId, MatchState(tick, newEntities))
          yield (departed ++ leaving, ())
      .take(ticks)
      .as(())

object MatchRunner:
  /** The server ticks at roughly 60 frames per second. */
  val TickInterval: FiniteDuration = 16.millis

  /** How long a match lasts when no other duration is given. */
  val DefaultDuration: FiniteDuration = 60.seconds
