package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput

/** Inbound port handling the core gameplay commands during an active match.
 *  Collects the inputs sent by the players, to be applied to the game world of their match.
 *
 *  @tparam F The effect type (e.g., IO)
 */
trait CommandPort[F[_]]:

  /** Processes a gameplay command sent by a player within a specific match.
   *
   *  @param matchId  The match where the action occurs.
   *  @param playerId The player performing the action.
   *  @param command  The raw input sent by the player (e.g., rotate, shoot).
   */
  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerInput): F[Unit]
