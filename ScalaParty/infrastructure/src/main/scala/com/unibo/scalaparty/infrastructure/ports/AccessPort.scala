package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

trait AccessPort[F[_]]:
  def joinLobby(playerId: PlayerId): F[MatchId]
  def leaveLobby(matchId: MatchId, playerId: PlayerId): F[Unit]
