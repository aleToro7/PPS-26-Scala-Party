package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, CommandPort}

class WebSocketServer(
                       connections: ConnectionRegistry,
                       accessPort: AccessPort[IO],
                       commandPort: CommandPort[IO]
                     ):

  def onConnect(playerId: PlayerId): IO[MatchId] =
    for
      matchId <- accessPort.joinLobby(playerId)
      _       <- connections.bindSessionToMatch(playerId, matchId)
      _       <- IO.println(s"Player $playerId assigned to the match $matchId")
    yield matchId

  def onDisconnect(matchId: MatchId, playerId: PlayerId): IO[Unit] =
    for
      _ <- accessPort.leaveLobby(matchId, playerId)
      _ <- connections.removeSession(playerId)
      _ <- IO.println(s"Player $playerId disconnected")
    yield ()

  def onMessage(playerId: PlayerId, matchId: MatchId, frame: WebSocketFrame): IO[Unit] =
    IO.println(s"Message from $playerId: $frame")
  // TODO parsing JSON in RFU3 -> Implementazione invio comandi

  def routes(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root / "ws" =>
      val playerId = PlayerId.random()

      for
        matchId <- onConnect(playerId)

        response <- wsb
          .withOnClose(onDisconnect(matchId, playerId))
          .build(
            send = Stream.never[IO],
            receive = stream => stream.evalMap(frame => onMessage(playerId, matchId, frame))
          )
      yield response