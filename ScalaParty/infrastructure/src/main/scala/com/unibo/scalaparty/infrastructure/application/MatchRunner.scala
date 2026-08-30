package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.IO
import fs2.Stream
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.infrastructure.application.CommandAdapter.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

trait MatchEventPublisher[F[_]]:
  def publishState(matchId: MatchId, entities: List[EntityDto]): F[Unit]

type PlayerEntityMapping = Map[PlayerId, EntityId]

case class MatchSession(
    matchId: MatchId,
    players: PlayerEntityMapping,
    world: GameWorld
)

class MatchRunner(
    session: MatchSession,
    commandQueue: GameCommandService,
    engine: GameEngine,
    publisher: MatchEventPublisher[IO]
):

  def run: Stream[IO, Unit] =
    Stream.fixedRate[IO](16.millis).zipWithIndex.evalMap: (_, tick) =>
      for
        // Drain the raw commands from the infrastructure queue
        rawCommands <- commandQueue.drainCommands(session.matchId)

        // Purely resolve network intents into ECS domain commands using the MatchSession mapping
        ecsCommands = rawCommands.flatMap: (playerId, intent) =>
          session.players.get(playerId).flatMap(intent.toDto)

        // Process the resolved commands in the game engine
        newEntities = engine.update(ecsCommands, 16L)

        // Publish the new authoritative state
        _ <- publisher.publishState(session.matchId, newEntities)
      yield ()
