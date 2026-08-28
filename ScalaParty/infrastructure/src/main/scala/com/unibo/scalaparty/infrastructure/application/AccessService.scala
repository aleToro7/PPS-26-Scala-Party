package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.ports.AccessPort
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

/**
 * Default implementation of the [[AccessPort]].
 *
 * It acts as an adapter layer that delegates the matchmaking and lobby lifecycle
 * operations directly to the purely functional [[LobbyManager]].
 *
 * @param lobby The core application state manager for pending and active matches.
 */
class AccessService(lobby: LobbyManager[IO]) extends AccessPort[IO]:

  def joinLobby(playerId: PlayerId): IO[MatchId] =
    lobby.joinLobby(playerId)

  def leaveLobby(matchId: MatchId, playerId: PlayerId): IO[Unit] =
    lobby.leaveLobby(matchId, playerId)