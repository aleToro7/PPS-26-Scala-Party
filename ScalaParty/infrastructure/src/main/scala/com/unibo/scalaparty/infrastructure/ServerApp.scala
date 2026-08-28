package com.unibo.scalaparty.infrastructure

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import com.unibo.scalaparty.infrastructure.application.{LobbyManager, AccessService, GameCommandService}
import com.unibo.scalaparty.infrastructure.network.{ConnectionRegistry, WebSocketServer}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2

object ServerApp extends IOApp.Simple:

  val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root =>
      Ok("Scala Party Server is up and running!")

  def httpApp(wsb: WebSocketBuilder2[IO], wsServer: WebSocketServer) = Router(
    "/" -> baseRoute,
    "/" -> wsServer.routes(wsb)
  ).orNotFound

  val run: IO[Unit] =
    for
      _        <- IO.println("Initializing services...")
      registry <- ConnectionRegistry()
      lobby    <- LobbyManager.of[IO]

      accessService  = AccessService(lobby)
      commandService = GameCommandService()

      // Injecting both ports into the network adapter
      wsServer = WebSocketServer(registry, accessService, commandService)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpWebSocketApp(wsb => httpApp(wsb, wsServer))
        .build
        .use(_ => IO.println("Server started on port 8081") *> IO.never)
    yield ()