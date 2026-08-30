package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

/** Inbound port handling the matchmaking and lobby phase of the game.
 *  Responsible for managing the logical lifecycle of a player joining or leaving a match structure.
 *
 *  @tparam F The effect type (e.g., IO)
 */
trait AccessPort[F[_]]:
  /** Assigns a player to an available match or creates a new one if none are pending.
   *
   *  @param playerId The unique identifier of the connecting player.
   *  @return The ID of the match the player has been assigned to.
   */
  def joinLobby(playerId: PlayerId): F[MatchId]

  /** Removes a player from a match. If the match becomes empty, it is disbanded.
   *
   *  @param matchId  The match the player is currently in.
   *  @param playerId The player leaving the match.
   */
  def leaveLobby(matchId: MatchId, playerId: PlayerId): F[Unit]
