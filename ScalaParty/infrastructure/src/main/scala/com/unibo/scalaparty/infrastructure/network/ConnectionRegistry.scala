package com.unibo.scalaparty.infrastructure.network

import cats.effect.{IO, Ref}
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

/**
 * Registry for managing active physical connections (e.g., WebSockets).
 * Tracks the relationship between a connected player and their assigned match,
 * enabling targeted message broadcasting to specific groups of clients.
 */
trait ConnectionRegistry:

  /** Links an active physical session of a player to a logical match. */
  def bindSessionToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit]

  /** Removes a physical session from the registry upon disconnection. */
  def removeSession(playerId: PlayerId): IO[Unit]

  /**
   * Retrieves all currently connected players for a given match.
   * Essential for broadcasting game state updates only to the relevant clients.
   *
   * @param matchId The match to query.
   * @return A list of players currently holding an active connection in that match.
   */
  def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]]

object ConnectionRegistry:

  private type RegistryState = Map[PlayerId, MatchId]

  private class ConnectionRegistryImpl(state: Ref[IO, RegistryState]) extends ConnectionRegistry:

    def bindSessionToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit] =
      state.update(_ + (playerId -> matchId))

    def removeSession(playerId: PlayerId): IO[Unit] =
      state.update(_ - playerId)

    def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]] =
      state.get.map: stateMap =>
        stateMap.collect:
          case (pId, mId) if mId == matchId => pId
        .toList
          
  def apply(): IO[ConnectionRegistry] =
    Ref.of[IO, RegistryState](Map.empty).map(ref => ConnectionRegistryImpl(ref))