package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.model.PlayerCommand

/**
 * Inbound port handling the core gameplay commands during an active match.
 * Routes player inputs to the underlying game engine for state computation.
 *
 * @tparam F The effect type (e.g., IO)
 */
trait CommandPort[F[_]]:

  /**
   * Processes a gameplay command sent by a player within a specific match.
   *
   * @param matchId  The match where the action occurs.
   * @param playerId The player performing the action.
   * @param command  The specific domain command (e.g., move, shoot).
   */
  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): F[Unit]
  
