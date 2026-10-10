package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{Admission, PlayerId}

/** Inbound port handling the matchmaking and lobby phase of the game.
 *  Responsible for the logical lifecycle of a player entering or leaving the game.
 *
 *  Joining does not necessarily grant a match: a player may well end up waiting for its turn, and
 *  whatever it needs to know about that is delivered through [[PlayerNotifier]]. The only thing
 *  returned is whether the player was taken in at all, since turning it away means closing its
 *  connection, which only the adapter holding that connection can do.
 *
 *  @tparam F The effect type (e.g., IO)
 */
trait AccessPort[F[_]]:

  /** Takes a player in, either admitting it to a match that begins right away or queueing it, or
   *  turns it away when there is no room left for it.
   *
   *  @param playerId The unique identifier of the connecting player.
   *  @return Whether the player was taken in.
   */
  def joinLobby(playerId: PlayerId): F[Admission]

  /** Drops a player, whether it was waiting for its turn or taking part in a match.
   *
   *  @param playerId The player leaving the game.
   */
  def leaveLobby(playerId: PlayerId): F[Unit]
