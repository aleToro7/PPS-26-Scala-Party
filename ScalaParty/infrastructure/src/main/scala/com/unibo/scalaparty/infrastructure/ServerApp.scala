package com.unibo.scalaparty.infrastructure

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.infrastructure.application.{GameCommandService, MatchCoordinator, QueuedLobbyManager}
import com.unibo.scalaparty.infrastructure.network.{
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

object ServerApp extends IOApp.Simple:
  private val gameRoute = "scalaparty"

  /** How many players a match is played by.
   *
   *  Kept at one on purpose: the only engine available is `SinglePlayerGameEngine`, which spawns a
   *  spaceship for `config.players.head` alone. Raising this would put several players in the same
   *  match while only the first of them gets a ship to fly.
   */
  private val PlayersPerMatch = 1

  /** How many matches the server plays at the same time; whoever arrives beyond them waits in the queue. */
  private val MaxConcurrentMatches = 2

  /** How many players can wait for a free room at the same time; whoever arrives beyond them is turned away. */
  private val MaxQueuedPlayers = 1

  private val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case request @ GET -> Root / gameRoute =>
      StaticFile
        .fromResource("/public/index.html", Some(request))
        .getOrElseF(NotFound())

    case GET -> Root =>
      Ok("Scala Party Server is up and running!")

  def httpApp(wsb: WebSocketBuilder2[IO], wsServer: WebSocketServer) = Router(
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

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpWebSocketApp(wsb => httpApp(wsb, wsServer))
        .build
        .use(_ => IO.println("Server started on port 8081") *> IO.never)
    yield ()
