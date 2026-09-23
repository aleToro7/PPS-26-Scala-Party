package com.unibo.scalaparty.infrastructure

import scala.concurrent.duration.*

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.infrastructure.application.{GameCommandService, MatchCoordinator, QueuedLobbyManager}
import com.unibo.scalaparty.infrastructure.model.PlayerId
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
import org.http4s.websocket.WebSocketFrame
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString

class ServerIntegrationSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers:

  "The integrated WebSocket Server" - {
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

        matches <- lobby.activeMatches
      yield
        response.status shouldBe Status.NotImplemented
        matches.map(_.players.size) shouldBe Set(1)
    )

    "should tell a player there is no room for it and close its connection" in {
      val playing = PlayerId.random()
      val rejected = PlayerId.random()
      for
        registry       <- ConnectionRegistry()
        lobby          <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxQueued = 0)
        commandService <- GameCommandService()

        notifier = WebSocketNotifier(registry)
        publisher = WebSocketBroadcaster(registry)
        settings = GameSettings.default

        coordinator <- MatchCoordinator(lobby, registry, commandService, notifier, publisher, settings, 10.seconds)

        wsServer = WebSocketServer(registry, coordinator, commandService)

        playingQueue  <- Queue.unbounded[IO, WebSocketFrame]
        rejectedQueue <- Queue.unbounded[IO, WebSocketFrame]
        _             <- wsServer.onConnect(playing, playingQueue)
        _             <- wsServer.onConnect(rejected, rejectedQueue)

        frames  <- rejectedQueue.tryTakeN(None)
        session <- registry.queueFor(rejected)
      yield
        frames.collect { case WebSocketFrame.Text(text, _) => text } shouldBe List("""{"QueueFull":{}}""")
        session shouldBe None
        frames.last match
          case close: WebSocketFrame.Close => close.closeCode shouldBe WebSocketServer.TryAgainLater
          case other => fail(s"expected the connection to be closed, got $other")
    }
  }
