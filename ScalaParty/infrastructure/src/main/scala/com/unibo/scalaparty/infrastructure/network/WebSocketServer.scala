package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import cats.effect.std.Queue
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import io.circe.generic.auto.*
import io.circe.parser.decode
import com.unibo.scalaparty.infrastructure.model.PlayerId
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, CommandPort}

/** Network adapter providing the WebSocket HTTP routes.
 *  Orchestrates the connection lifecycle by bridging physical socket events
 *  (Connect, Disconnect, Message) with the application's core logic ports.
 *
 *  A connection is no longer tied to a match for its whole life: a player may connect while another
 *  match is being played, wait its turn, and only then be assigned to one. The match a message
 *  belongs to is therefore looked up at every message rather than captured once at connection time.
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

  /** Handles a new player connection by registering their outbound message queue
   *  and admitting them into the matchmaking access port.
   *
   *  @param playerId the unique identifier of the connecting player
   *  @param queue    the concurrent queue used to push outbound WebSocket frames to the client
   *  @return an effect completing when the connection setup is finished
   */
  def onConnect(playerId: PlayerId, queue: MessageQueue): IO[Unit] =
    for
      _ <- connections.register(playerId, queue)
      _ <- accessPort.joinLobby(playerId)
      _ <- IO.println(s"Player $playerId connected")
    yield ()

  /** Handles player disconnection by removing their active session from the registry
   *  and notifying the access port that they have left the lobby.
   *
   *  @param playerId the unique identifier of the disconnecting player
   *  @return an effect completing when the cleanup operations finish
   */
  def onDisconnect(playerId: PlayerId): IO[Unit] =
    for
      _ <- connections.removeSession(playerId)
      _ <- accessPort.leaveLobby(playerId)
      _ <- IO.println(s"Player $playerId disconnected")
    yield ()

  /** Processes an incoming WebSocket frame received from a player, decoding valid inputs
   *  and routing them to the command port if the player is currently assigned to an active match.
   *
   *  @param playerId the unique identifier of the message sender
   *  @param frame    the raw WebSocket frame received from the client
   *  @return an effect completing when the message handling is done
   */
  def onMessage(playerId: PlayerId, frame: WebSocketFrame): IO[Unit] =
    frame match
      case WebSocketFrame.Text(jsonText, _) =>
        decode[PlayerInput](jsonText) match
          case Right(command) =>
            connections.matchOf(playerId).flatMap:
              case Some(matchId) => commandPort.handleCommand(matchId, playerId, command)
              case None => IO.println(s"Input from $playerId dropped: it is still waiting for a match")

          case Left(error) =>
            IO.println(s"Invalid JSON received from $playerId: ${error.getMessage}")

      case _ => IO.unit

  /** Creates the http4s HTTP routes handling the WebSocket endpoint at `/ws`.
   *
   *  @param wsb the WebSocket builder used to construct the socket response
   *  @return the configured HttpRoutes for the application
   */
  def routes(wsb: WebSocketBuilder2[IO]): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root / "ws" =>
      val playerId = PlayerId.random()

      for
        // Create an unbounded concurrent queue for outbound messages
        outboundQueue <- Queue.unbounded[IO, WebSocketFrame]

        _ <- onConnect(playerId, outboundQueue)

        response <- wsb
          .withOnClose(onDisconnect(playerId))
          .build(
            // Pipe the queue directly into the outbound WebSocket stream
            send = Stream.fromQueueUnterminated(outboundQueue),
            receive = stream => stream.evalMap(frame => onMessage(playerId, frame))
          )
      yield response
