package com.unibo.scalaparty.infrastructure.application

import cats.effect.{IO, Ref}
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.ports.CommandPort

type CommandBuffer = Map[MatchId, List[(PlayerId, PlayerInput)]]

/** Implementation of the [[CommandPort]] responsible for buffering in-game network actions.
 *  It routes commands purely based on player and match IDs without resolving game logic.
 */
class GameCommandService(bufferRef: Ref[IO, CommandBuffer]) extends CommandPort[IO]:

  /** Handles and buffers an incoming gameplay command from a player within a specific match.
   *
   * Updates the concurrent state buffer atomically by appending the player's input
   * to the match's pending command queue, then logs the buffered action.
   *
   * @param matchId  the match where the action occurs
   * @param playerId the player performing the action
   * @param command  the specific player input/command to buffer
   * @return an IO effect completing when the command is safely buffered and logged
   */
  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerInput): IO[Unit] =
    bufferRef.update: buffer =>
      val currentCommands = buffer.getOrElse(matchId, List.empty)
      buffer.updated(matchId, currentCommands :+ (playerId -> command))
    .flatMap(_ =>
      IO.println(s"Match $matchId | Command buffered from player $playerId: $command")
    )

  /** Extracts all accumulated player inputs for a given match and atomically clears the queue.
   *
   * Retrieves the list of pending commands associated with the match ID and replaces
   * them with an empty list in a single atomic modification of the buffer state.
   *
   * @param matchId the match whose pending commands are to be extracted
   * @return an IO effect containing the list of accumulated player inputs for the match
   */
  def drainCommands(matchId: MatchId): IO[List[(PlayerId, PlayerInput)]] =
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
