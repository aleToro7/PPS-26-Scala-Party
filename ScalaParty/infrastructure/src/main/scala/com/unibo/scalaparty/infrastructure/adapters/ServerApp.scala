package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.{IO, IOApp}
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import fs2.{Pipe, Stream}

object ServerApp extends IOApp.Simple {

  val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok("Scala Party Server is up and running!")
  }

  def wsRoute(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "ws" =>
      val receive: Pipe[IO, WebSocketFrame, Unit] =
        _.evalMap(frame => IO.println(s"Ricevuto dal client: $frame"))

      val send: Stream[IO, WebSocketFrame] = Stream.never[IO]

      wsb.build(send, receive)
  }
  
  // Router che mappa la rotta sulla root "/"
  def httpApp(wsb: WebSocketBuilder2[IO]) = Router(
    "/" -> baseRoute,
    "/" -> wsRoute(wsb)
  ).orNotFound

  // Dopo 60s senza scambio di messaggi la connessione si chiude automaticamente
  val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpWebSocketApp(wsb => httpApp(wsb))
      .build
      .use(_ => IO.never)
}