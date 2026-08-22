package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.{IO, IOApp}
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import com.unibo.scalaparty.core.model.PlayerCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.GameCommandPort

object ServerApp extends IOApp.Simple {

  val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok("Scala Party Server is up and running!")
  }

  val dummyCommandPort: GameCommandPort[IO] = new GameCommandPort[IO] {
    def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): IO[Unit] =
      IO.println(s"Stub: Command recived $command from $playerId on match $matchId")

    def joinLobby(playerId: PlayerId): IO[MatchId] =
      IO.println(s"Stub: $playerId joined the lobby") *> IO.pure(MatchId.random())

    def leaveLobby(matchId: MatchId, playerId: PlayerId): IO[Unit] =
      IO.println(s"Stub: $playerId left the lobby $matchId")
  }

  def httpApp(wsb: WebSocketBuilder2[IO], wsServer: WebSocketServer) = Router(
    "/" -> baseRoute,
    "/" -> wsServer.routes(wsb)
  ).orNotFound

  val run: IO[Unit] =
    for {
      _ <- IO.println("Initializing services...")
      registry <- ConnectionRegistry.make()
      wsServer = new WebSocketServer(registry, dummyCommandPort)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8081")
        .withHttpWebSocketApp(wsb => httpApp(wsb, wsServer))
        .build
        .use(_ => IO.println("Server started on port 8081") *> IO.never)
    } yield ()
}