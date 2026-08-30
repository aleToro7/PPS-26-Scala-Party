package com.unibo.scalaparty.infrastructure.application

import cats.effect.{IO, Ref}
import com.unibo.scalaparty.core.dto.PlayerCommand as DtoCommand
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.infrastructure.application.CommandAdapter.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.CommandPort

type CommandBuffer = Map[MatchId, List[(PlayerId, IntentCommand)]]

/** Implementation of the [[CommandPort]] responsible for buffering in-game network actions.
 *  It routes commands purely based on player and match IDs without resolving game logic.
 */
class GameCommandService(bufferRef: Ref[IO, CommandBuffer]) extends CommandPort[IO]:

  def handleCommand(matchId: MatchId, playerId: PlayerId, command: IntentCommand): IO[Unit] =
    bufferRef.update: buffer =>
      val currentCommands = buffer.getOrElse(matchId, List.empty)
      buffer.updated(matchId, currentCommands :+ (playerId -> command))
    .flatMap(_ =>
      IO.println(s"Match $matchId | Command buffered from player $playerId: $command")
    )

  /** Extracts all accumulated (PlayerId, IntentCommand) for a match and clears the queue. */
  def drainCommands(matchId: MatchId): IO[List[(PlayerId, IntentCommand)]] =
    bufferRef.modify: buffer =>
      val pending = buffer.getOrElse(matchId, List.empty)
      (buffer.updated(matchId, List.empty), pending)

object GameCommandService:
  /** Factory method that safely initializes the concurrent state buffer.
   *
   *  @return an IO containing the instantiated GameCommandService.
   */
  def apply(): IO[GameCommandService] =
    Ref.of[IO, CommandBuffer](Map.empty).map(new GameCommandService(_))
