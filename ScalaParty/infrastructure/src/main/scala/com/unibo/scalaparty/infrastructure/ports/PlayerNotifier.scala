package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{PlayerId, ServerMessage}

/** Outbound port delivering a lobby notification to one specific player.
 *
 *  Unlike [[MatchEventPublisher]], which addresses everybody taking part in a match, this port
 *  targets a single connection: a player waiting in the queue has no match to be addressed through.
 *
 *  @tparam F The effect type (e.g., IO)
 */
trait PlayerNotifier[F[_]]:

  /** Delivers the message to the given player, doing nothing if it is no longer connected.
   *
   *  @param playerId The recipient of the message.
   *  @param message  What the player is being told.
   */
  def send(playerId: PlayerId, message: ServerMessage): F[Unit]
