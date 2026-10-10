package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import io.circe.syntax.*
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.model.{PlayerId, ServerMessage}
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import com.unibo.scalaparty.infrastructure.ports.PlayerNotifier

/** Outbound adapter implementing the [[PlayerNotifier]] port over WebSockets.
 *  Serializes the message to JSON and pushes it into the outbound queue of that single player.
 */
class WebSocketNotifier(registry: ConnectionRegistry) extends PlayerNotifier[IO]:

  override def send(playerId: PlayerId, message: ServerMessage): IO[Unit] =
    registry.queueFor(playerId).flatMap:
      case Some(queue) => queue.offer(WebSocketFrame.Text(message.asJson.noSpaces))
      case None => IO.unit
