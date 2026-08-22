package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.{IO, Ref}
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

class ConnectionRegistry(sessionMap: Ref[IO, Map[PlayerId, PlayerInfo]]) {

  def bindSessionToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit] =
    sessionMap.update { map =>
      map.updated(playerId, PlayerInfo(playerId, Some(matchId)))
    }

  def removeSession(playerId: PlayerId): IO[Unit] =
    sessionMap.update(map => map - playerId)

  def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]] =
    sessionMap.get.map { map =>
      map.values
         .filter(_.matchId.contains(matchId))
         .map(_.playerId)
         .toList
    }
}

case class PlayerInfo(playerId: PlayerId, matchId: Option[MatchId])

object ConnectionRegistry:
  // Costruttore per inizializzare il Ref vuoto
  def make(): IO[ConnectionRegistry] =
    Ref.of[IO, Map[PlayerId, PlayerInfo]](Map.empty).map(new ConnectionRegistry(_))