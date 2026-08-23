package com.unibo.scalaparty.infrastructure

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.infrastructure.adapters.{ConnectionRegistry, WebSocketServer}
import com.unibo.scalaparty.infrastructure.application.LobbyManager
import com.unibo.scalaparty.core.model.PlayerCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.GameCommandPort
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.implicits.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString

class ServerIntegrationSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "The integrated WebSocket Server" - {
    "should handle a connection request and assign the player to a lobby" in {
      for {
        registry <- ConnectionRegistry.make()
        lobby    <- LobbyManager.of[IO]

        commandPort = new GameCommandPort[IO] {
          def joinLobby(playerId: PlayerId): IO[MatchId] = lobby.joinLobby(playerId)
          def leaveLobby(matchId: MatchId, playerId: PlayerId): IO[Unit] = lobby.leaveLobby(matchId, playerId)
          def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): IO[Unit] = IO.unit
        }

        wsServer = new WebSocketServer(registry, commandPort)

        // HTTP request simulating the headers of WebSocket client
        request = Request[IO](method = GET, uri = uri"/ws")
          .withHeaders(
            Header.Raw(CIString("Connection"), "Upgrade"),
            Header.Raw(CIString("Upgrade"), "websocket"),
            Header.Raw(CIString("Sec-WebSocket-Version"), "13"),
            Header.Raw(CIString("Sec-WebSocket-Key"), "dGhlIHNhbXBsZSBub25jZQ==")
          )

        wsb      <- WebSocketBuilder2[IO]
        response <- wsServer.routes(wsb).orNotFound.run(request)

        activeMatches <- lobby.activeMatchIds

      } yield {
        response.status shouldBe Status.NotImplemented

        activeMatches.size shouldBe 1
      }
    }
  }
}