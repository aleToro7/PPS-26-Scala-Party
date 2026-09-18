package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.IO
import fs2.Stream
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.model.MatchState
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
 *  The stream is finite. There being no win condition in the game yet (no collisions, no health),
 *  a match simply lasts [[duration]] and then ends, which is what lets the waiting queue move on.
 */
class MatchRunner(
    session: MatchSession,
    commandQueue: GameCommandService,
    engine: GameEngine,
    publisher: MatchEventPublisher[IO],
    duration: FiniteDuration = MatchRunner.DefaultDuration
):

  /** How many ticks fit in the match duration. */
  private val ticks: Long = (duration / MatchRunner.TickInterval).toLong

  def run: Stream[IO, Unit] =
    Stream
      .fixedRate[IO](MatchRunner.TickInterval)
      .zipWithIndex
      .evalMap: (_, tick) =>
        for
          // Drain the raw commands from the infrastructure queue
          rawCommands <- commandQueue.drainCommands(session.matchId)

          // Purely resolve network intents into ECS domain commands using the MatchSession mapping
          ecsCommands = rawCommands.flatMap: (playerId, intent) =>
            session.players.get(playerId).flatMap(intent.toDto)

          // Process the resolved commands in the game engine
          newEntities = engine.update(ecsCommands, MatchRunner.TickInterval.toMillis)

          // Publish the new authoritative state
          _ <- publisher.broadcastState(session.matchId, MatchState(tick, newEntities))
        yield ()
      .take(ticks)

object MatchRunner:
  /** The server ticks at roughly 60 frames per second. */
  val TickInterval: FiniteDuration = 16.millis

  /** How long a match lasts when no other duration is given. */
  val DefaultDuration: FiniteDuration = 60.seconds
