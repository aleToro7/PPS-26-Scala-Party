package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.IO
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.ports.GameCommandPort

class WebSocketServer(
 connections: ConnectionRegistry,
 commandPort: GameCommandPort[IO]
) {
  def onConnect(): IO[Unit] =
    IO.println("onConnect: new client connected")

  def onDisconnect(): IO[Unit] =
    IO.println("onDisconnect: client closed connection")

  def onMessage(frame: WebSocketFrame): IO[Unit] =
    IO.println(s"onMessage: $frame")
    //TODO parsing JSON

  def routes(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "ws" =>
      val receive: Pipe[IO, WebSocketFrame, Unit] = stream =>
        stream
          .evalMap(frame => onMessage(frame))
          .onFinalize(onDisconnect())

      val send: Stream[IO, WebSocketFrame] = Stream.never[IO]
      
      for {
        _ <- onConnect()
        response <- wsb.build(send, receive)
      } yield response
  }
}
