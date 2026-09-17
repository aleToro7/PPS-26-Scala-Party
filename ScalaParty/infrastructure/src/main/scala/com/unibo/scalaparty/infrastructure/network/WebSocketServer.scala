package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, CommandPort}
import io.circe.generic.auto.*
import io.circe.parser.decode
import cats.effect.std.Queue

/** Network adapter providing the WebSocket HTTP routes.
 *  Orchestrates the connection lifecycle by bridging physical socket events
 *  (Connect, Disconnect, Message) with the application's core logic ports.
 *
 *  @param connections Registry to track active sockets for future broadcasting.
 *  @param accessPort Service handling the logical assignment of players to matches.
 *  @param commandPort Service handling gameplay inputs (e.g., moving, shooting).
 */
class WebSocketServer(
    connections: ConnectionRegistry,
    accessPort: AccessPort[IO],
    commandPort: CommandPort[IO]
):

  def onConnect(playerId: PlayerId, queue: MessageQueue): IO[MatchId] =
    for
      matchId <- accessPort.joinLobby(playerId)
      _       <- connections.bindSessionToMatch(playerId, matchId, queue)
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
        decode[PlayerInput](jsonText) match
          case Right(command) =>
            commandPort.handleCommand(matchId, playerId, command)

          case Left(error) =>
            IO.println(s"Invalid JSON received from $playerId: ${error.getMessage}")

      case _ => IO.unit

  def routes(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root / "ws" =>
      val playerId = PlayerId.random()

      for
        // Create an unbounded concurrent queue for outbound messages
        outboundQueue <- Queue.unbounded[IO, WebSocketFrame]

        matchId <- onConnect(playerId, outboundQueue)

        response <- wsb
          .withOnClose(onDisconnect(matchId, playerId))
          .build(
            // Pipe the queue directly into the outbound WebSocket stream
            send = Stream.fromQueueUnterminated(outboundQueue),
            receive = stream => stream.evalMap(frame => onMessage(playerId, matchId, frame))
          )
      yield response
