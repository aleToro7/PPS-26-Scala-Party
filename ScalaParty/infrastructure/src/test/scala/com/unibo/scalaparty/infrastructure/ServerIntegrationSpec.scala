package com.unibo.scalaparty.infrastructure

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.infrastructure.application.{LobbyManager, AccessService, GameCommandService}
import com.unibo.scalaparty.infrastructure.network.{ConnectionRegistry, WebSocketServer}
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.implicits.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString

class ServerIntegrationSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers:

  "The integrated WebSocket Server" - (
    "should handle a connection request and assign the player to a lobby" in (

      for
        registry <- ConnectionRegistry()
        lobby    <- LobbyManager.of[IO]

        accessService  = AccessService(lobby)
        commandService <- GameCommandService()

        wsServer = WebSocketServer(registry, accessService, commandService)

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

      yield
        response.status shouldBe Status.NotImplemented
        activeMatches.size shouldBe 1
      )
    )