package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.PlayerId

/** Inbound port handling the matchmaking and lobby phase of the game.
 *  Responsible for the logical lifecycle of a player entering or leaving the game.
 *
 *  Joining does not necessarily grant a match: a player may well end up waiting for its turn, so
 *  nothing is returned here. Whatever the player needs to know is delivered through
 *  [[PlayerNotifier]] instead.
 *
 *  @tparam F The effect type (e.g., IO)
 */
trait AccessPort[F[_]]:

  /** Takes a player in, either admitting it to a match that begins right away or queueing it.
   *
   *  @param playerId The unique identifier of the connecting player.
   */
  def joinLobby(playerId: PlayerId): F[Unit]

  /** Drops a player, whether it was waiting for its turn or taking part in a match.
   *
   *  @param playerId The player leaving the game.
   */
  def leaveLobby(playerId: PlayerId): F[Unit]
