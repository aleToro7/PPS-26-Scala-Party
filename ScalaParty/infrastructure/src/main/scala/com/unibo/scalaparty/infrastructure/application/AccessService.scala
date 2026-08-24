package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.ports.AccessPort
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

class AccessService(lobby: LobbyManager[IO]) extends AccessPort[IO]:

  def joinLobby(playerId: PlayerId): IO[MatchId] =
    lobby.joinLobby(playerId)

  def leaveLobby(matchId: MatchId, playerId: PlayerId): IO[Unit] =
    lobby.leaveLobby(matchId, playerId)