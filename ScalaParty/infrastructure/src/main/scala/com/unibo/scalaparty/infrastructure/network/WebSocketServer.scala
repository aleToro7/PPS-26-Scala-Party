package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, CommandPort}
import com.unibo.scalaparty.core.model.PlayerCommand
import io.circe.parser.decode
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given

/**
 * Network adapter providing the WebSocket HTTP routes.
 * Orchestrates the connection lifecycle by bridging physical socket events
 * (Connect, Disconnect, Message) with the application's core logic ports.
 *
 * @param connections Registry to track active sockets for future broadcasting.
 * @param accessPort Service handling the logical assignment of players to matches.
 * @param commandPort Service handling gameplay inputs (e.g., moving, shooting).
 */
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
    frame match
      case WebSocketFrame.Text(jsonText, _) =>
        decode[PlayerCommand](jsonText) match
          case Right(command) =>
            commandPort.handleCommand(matchId, playerId, command)

          case Left(error) =>
            IO.println(s"Invalid JSON received from $playerId: ${error.getMessage}")

      case _ => IO.unit

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