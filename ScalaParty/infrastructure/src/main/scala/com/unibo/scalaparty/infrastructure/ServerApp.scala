package com.unibo.scalaparty.infrastructure

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.infrastructure.application.{GameCommandService, MatchCoordinator, QueuedLobbyManager}
import com.unibo.scalaparty.infrastructure.network.{
  ClientDisconnectionLogger,
  ConnectionRegistry,
  WebSocketBroadcaster,
  WebSocketNotifier,
  WebSocketServer
}
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.slf4j.Slf4jLogger

object ServerApp extends IOApp.Simple:
  private val gameRoute = "scalaparty"

  /** How many players a match is played by: a match begins as soon as that many are waiting. */
  private val PlayersPerMatch = 2

  /** How many matches the server plays at the same time; whoever arrives beyond them waits in the queue. */
  private val MaxConcurrentMatches = 2

  /** How many players can wait for a free room at the same time; whoever arrives beyond them is turned away. */
  private val MaxQueuedPlayers = PlayersPerMatch

  private val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case request @ GET -> Root / gameRoute =>
      StaticFile
        .fromResource("/public/index.html", Some(request))
        .getOrElseF(NotFound())

    case GET -> Root =>
      Ok("Scala Party Server is up and running!")

  private def httpApp(wsb: WebSocketBuilder2[IO], wsServer: WebSocketServer) = Router(
    "/" -> baseRoute,
    s"/$gameRoute" -> wsServer.routes(wsb)
  ).orNotFound

  val run: IO[Unit] =
    for
      _        <- IO.println("Initializing services...")
      registry <- ConnectionRegistry()
      lobby    <- QueuedLobbyManager.of[IO](
        minPlayers = PlayersPerMatch,
        maxPlayers = PlayersPerMatch,
        maxMatches = MaxConcurrentMatches,
        maxQueued = MaxQueuedPlayers
      )
      commandService <- GameCommandService()

      notifier = WebSocketNotifier(registry)
      publisher = WebSocketBroadcaster(registry)
      settings = GameSettings.default

      coordinator <- MatchCoordinator(lobby, registry, commandService, notifier, publisher, settings)

      wsServer = WebSocketServer(registry, coordinator, commandService)
      serverLogger = ClientDisconnectionLogger(Slf4jLogger.getLoggerFromClass[IO](classOf[EmberServerBuilder[IO]]))

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withLogger(serverLogger)
        .withHttpWebSocketApp(wsb => httpApp(wsb, wsServer))
        .build
        .use(_ => IO.println("Server started on port 8081") *> IO.never)
    yield ()
