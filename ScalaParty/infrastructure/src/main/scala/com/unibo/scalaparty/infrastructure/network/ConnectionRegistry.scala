package com.unibo.scalaparty.infrastructure.network

import cats.effect.{IO, Ref}
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

trait ConnectionRegistry:
  def bindSessionToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit]
  def removeSession(playerId: PlayerId): IO[Unit]
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