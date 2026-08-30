package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s.websocket.WebSocketFrame
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import com.unibo.scalaparty.core.model.MatchState
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

class WebSocketBroadcasterSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "WebSocketBroadcaster".should:

    "retrieve active client queues from the registry and broadcast the MatchState".in:
      val matchId = MatchId.random()
      val playerId = PlayerId.random()
      val dummyState = MatchState(tick = 1L, entities = List.empty)

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.bindSessionToMatch(playerId, matchId, queue)

        broadcaster = WebSocketBroadcaster(registry)

        _ <- broadcaster.broadcastState(matchId, dummyState)

        frame <- queue.tryTake
      yield
        frame shouldBe defined
        frame.get match
          case WebSocketFrame.Text(text, _) => text should include(""""tick":1""")
          case _                            => fail("Expected a Text WebSocket frame")