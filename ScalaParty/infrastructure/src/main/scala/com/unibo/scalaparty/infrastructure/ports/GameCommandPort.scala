package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.core.model.PlayerCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

trait GameCommandPort[F[_]]:
  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): F[Unit]
  def joinLobby(playerId: PlayerId): F[MatchId]
  def leaveLobby(matchId: MatchId, playerId: PlayerId): F[Unit]
