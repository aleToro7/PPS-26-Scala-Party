package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import cats.syntax.all.*
import io.circe.syntax.*
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.core.model.{GameEvent, MatchState}
import com.unibo.scalaparty.infrastructure.model.MatchId
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import com.unibo.scalaparty.infrastructure.ports.MatchEventPublisher

/** Outbound adapter that implements the [[MatchEventPublisher]] port.
 *  It serializes the pure domain events and match states into JSON strings,
 *  then pushes them to the active WebSocket message queues retrieved from the registry.
 */
class WebSocketBroadcaster(registry: ConnectionRegistry) extends MatchEventPublisher[IO]:

  override def broadcastState(matchId: MatchId, state: MatchState): IO[Unit] =
    val frame = WebSocketFrame.Text(state.asJson.noSpaces)
    broadcastToMatch(matchId, frame)

  override def broadcastEvent(matchId: MatchId, event: GameEvent): IO[Unit] =
    val frame = WebSocketFrame.Text(event.asJson.noSpaces)
    broadcastToMatch(matchId, frame)

  private def broadcastToMatch(matchId: MatchId, frame: WebSocketFrame): IO[Unit] =
    // Queues are unbounded, so offering never blocks and a sequential push is enough
    registry.getQueuesForMatch(matchId).flatMap(_.traverse_(_.offer(frame)))
