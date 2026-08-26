package com.unibo.scalaparty.infrastructure.application

import cats.effect.{IO, Ref}
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.ports.CommandPort
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.core.dto.PlayerCommand as DtoCommand
import com.unibo.scalaparty.infrastructure.application.CommandAdapter.*

type CommandBuffer = Map[MatchId, List[DtoCommand]]

/**
 * Implementation of the [[CommandPort]] responsible for routing in-game actions.
 *
 * Note: Currently implemented as a stub for RFU1. In future iterations (e.g., RFU3),
 * this service will take the GameEngine as a dependency to process actual
 * gameplay mechanics and resolve state computations.
 */
class GameCommandService(bufferRef: Ref[IO, CommandBuffer]) extends CommandPort[IO]:

  def handleCommand(matchId: MatchId, playerId: PlayerId, command: IntentCommand): IO[Unit] =

    // TODO: get EntityId
    val dummyEntityId = EntityId.generate()

    command.toDto(dummyEntityId) match
      case Some(ecsCommand) =>
        bufferRef.update(buffer =>
          val currentCommands = buffer.getOrElse(matchId, List.empty)
          buffer.updated(matchId, currentCommands :+ ecsCommand)
        ).flatMap(_ =>
          IO.println(s"Match $matchId | Command buffered: $ecsCommand")
        )

      case None =>
        IO.println(s"Match $matchId | Command ignored: $command is not supported by DTO")
    
object GameCommandService:
  def apply(): IO[GameCommandService] =
    Ref.of[IO, CommandBuffer](Map.empty).map(ref => new GameCommandService(ref))