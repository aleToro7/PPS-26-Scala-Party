package com.unibo.scalaparty.infrastructure.network

import scala.concurrent.duration.*

import cats.effect.IO
import cats.effect.std.Queue
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import io.circe.generic.auto.*
import io.circe.parser.decode
import com.unibo.scalaparty.infrastructure.model.{Admission, PlayerId}
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
 *  A player may also stay silent for a long time, waiting in the queue or simply not touching the
 *  keys, while the server drops any connection it hears nothing from for a while. The connection of
 *  every admitted player is therefore pinged at regular intervals: the pong the client answers with
 *  keeps it open.
 *
 *  @param connections Registry to track active sockets for future broadcasting.
 *  @param accessPort Service handling the logical assignment of players to matches.
 *  @param commandPort Service handling gameplay inputs (e.g., moving, shooting).
 *  @param keepAliveInterval How often each admitted player is pinged.
 */
class WebSocketServer(
    connections: ConnectionRegistry,
    accessPort: AccessPort[IO],
    commandPort: CommandPort[IO],
    keepAliveInterval: FiniteDuration = WebSocketServer.keepAliveInterval
):

  /** Handles a new player connection by registering their outbound message queue
   *  and admitting them into the matchmaking access port, or closing the connection if the access
   *  port turns them away.
   *
   *  @param playerId the unique identifier of the connecting player
   *  @param queue    the concurrent queue used to push outbound WebSocket frames to the client
   *  @return an effect reporting whether the player was taken in, once the connection setup is finished
   */
  def onConnect(playerId: PlayerId, queue: MessageQueue): IO[Admission] =
    for
      _         <- connections.register(playerId, queue)
      admission <- accessPort.joinLobby(playerId)
      _         <- admission match
        case Admission.Admitted => IO.println(s"Player $playerId connected")
        case Admission.Rejected => turnAway(playerId, queue)
    yield admission

  /** Closes the connection of a player there is no room for, once whatever it was told is delivered.
   *
   *  The session is dropped right away rather than on close, so the player is not addressed again
   *  whether or not the client completes the closing handshake.
   *
   *  @param playerId the unique identifier of the rejected player
   *  @param queue    the outbound queue of that player, to close the connection through
   *  @return an effect completing when the closing frame is queued
   */
  private def turnAway(playerId: PlayerId, queue: MessageQueue): IO[Unit] =
    for
      _     <- connections.removeSession(playerId)
      close <- IO.fromEither(WebSocketFrame.Close(WebSocketServer.tryAgainLater, "the queue is full"))
      _     <- queue.offer(close)
      _     <- IO.println(s"Player $playerId turned away: the queue is full")
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

  /** The frames sent to an admitted player: whatever the server queues for it, together with a ping
   *  every [[keepAliveInterval]] that keeps the connection open while the player has nothing to say.
   *
   *  @param queue the outbound queue of the player
   *  @return the never-ending stream of frames to write to the player's socket
   */
  def keptAlive(queue: MessageQueue): Stream[IO, WebSocketFrame] =
    Stream
      .fromQueueUnterminated(queue)
      .merge(Stream.awakeEvery[IO](keepAliveInterval).as(WebSocketFrame.Ping()))

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

        admission <- onConnect(playerId, outboundQueue)

        // A rejected player never entered the lobby and its session is already gone: leaving on close
        // would only make the lobby re-send their position to everybody waiting. Its connection is
        // being closed, so there is nothing to keep alive either.
        (onClose, send) = admission match
          case Admission.Admitted => (onDisconnect(playerId), keptAlive(outboundQueue))
          case Admission.Rejected =>
            (
              IO.println(s"Player $playerId closed after being turned away"),
              Stream.fromQueueUnterminated(outboundQueue)
            )

        response <- wsb
          .withOnClose(onClose)
          .build(
            send = send,
            receive = stream => stream.evalMap(frame => onMessage(playerId, frame))
          )
      yield response

object WebSocketServer:
  /** Close code telling the client the server is overloaded and it may retry later (RFC 6455, 7.4). */
  val tryAgainLater: Int = 1013

  /** How often a player is pinged when no other interval is given: a third of the 60 seconds after
   *  which the server drops a connection it hears nothing from, so a live client is never dropped.
   */
  val keepAliveInterval: FiniteDuration = 20.seconds
