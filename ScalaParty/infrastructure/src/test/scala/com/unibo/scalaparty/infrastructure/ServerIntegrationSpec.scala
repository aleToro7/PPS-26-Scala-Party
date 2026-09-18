package com.unibo.scalaparty.infrastructure

import scala.concurrent.duration.*

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.infrastructure.application.{GameCommandService, MatchCoordinator, QueuedLobbyManager}
import com.unibo.scalaparty.infrastructure.network.{
  ConnectionRegistry,
  WebSocketBroadcaster,
  WebSocketNotifier,
  WebSocketServer
}
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.implicits.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString

class ServerIntegrationSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers:

  "The integrated WebSocket Server" - (
    "should handle a connection request and assign the player to a match" in (
      for
        registry       <- ConnectionRegistry()
        lobby          <- QueuedLobbyManager.of[IO]()
        commandService <- GameCommandService()

        notifier = WebSocketNotifier(registry)
        publisher = WebSocketBroadcaster(registry)
        settings = GameSettings.default

        coordinator <- MatchCoordinator(lobby, registry, commandService, notifier, publisher, settings, 50.millis)

        wsServer = WebSocketServer(registry, coordinator, commandService)

        request = Request[IO](method = GET, uri = uri"/ws")
          .withHeaders(
            Header.Raw(CIString("Connection"), "Upgrade"),
            Header.Raw(CIString("Upgrade"), "websocket"),
            Header.Raw(CIString("Sec-WebSocket-Version"), "13"),
            Header.Raw(CIString("Sec-WebSocket-Key"), "dGhlIHNhbXBsZSBub25jZQ==")
          )

        wsb      <- WebSocketBuilder2[IO]
        response <- wsServer.routes(wsb).orNotFound.run(request)

        current <- lobby.currentMatch
      yield
        response.status shouldBe Status.NotImplemented
        current.map(_.players.size) shouldBe Some(1)
    )
  )
