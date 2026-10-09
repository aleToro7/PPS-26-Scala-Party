package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.http4s.websocket.WebSocketFrame
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class ConnectionRegistrySpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "A ConnectionRegistry".should:

    "allow binding a session to a match and retrieving it".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.bindSessionToMatch(playerId, matchId, queue)
        players  <- registry.getClientsForMatch(matchId)
      yield
        players.size shouldBe 1
        players.head shouldBe playerId

    "return an empty list for a match with no players".in:
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        players  <- registry.getClientsForMatch(matchId)
      yield players shouldBe empty

    "remove a session correctly".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.bindSessionToMatch(playerId, matchId, queue)
        _        <- registry.removeSession(playerId)
        players  <- registry.getClientsForMatch(matchId)
      yield players shouldBe empty

    "handle multiple players in the same match".in:
      val matchId = MatchId.random()
      val player1 = PlayerId.random()
      val player2 = PlayerId.random()

      for
        registry <- ConnectionRegistry()
        queue1   <- Queue.unbounded[IO, WebSocketFrame]
        queue2   <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.bindSessionToMatch(player1, matchId, queue1)
        _        <- registry.bindSessionToMatch(player2, matchId, queue2)
        players  <- registry.getClientsForMatch(matchId)
      yield
        players.size shouldBe 2
        players should contain allOf (player1, player2)

    "keep a registered player out of every match until it is assigned to one".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.register(playerId, queue)
        before   <- registry.matchOf(playerId)
        players  <- registry.getClientsForMatch(matchId)
      yield
        before shouldBe None
        players shouldBe empty

    "assign an already registered player to a match".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.register(playerId, queue)
        _        <- registry.assignToMatch(playerId, matchId)
        assigned <- registry.matchOf(playerId)
        players  <- registry.getClientsForMatch(matchId)
      yield
        assigned shouldBe Some(matchId)
        players shouldBe List(playerId)

    "ignore an assignment for a player that is not connected".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        _        <- registry.assignToMatch(playerId, matchId)
        assigned <- registry.matchOf(playerId)
      yield assigned shouldBe None

    "detach a player from its match while keeping the connection alive".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.bindSessionToMatch(playerId, matchId, queue)
        _        <- registry.clearMatch(playerId)
        current  <- registry.matchOf(playerId)
        players  <- registry.getClientsForMatch(matchId)
        outbound <- registry.queueFor(playerId)
      yield
        current shouldBe None
        players shouldBe empty
        outbound shouldBe defined

    "give access to the outbound queue of a single player".in:
      val playerId = PlayerId.random()
      val frame = WebSocketFrame.Text("hello")

      for
        registry <- ConnectionRegistry()
        queue    <- Queue.unbounded[IO, WebSocketFrame]
        _        <- registry.register(playerId, queue)
        outbound <- registry.queueFor(playerId)
        _        <- outbound.traverse_(_.offer(frame))
        received <- queue.tryTake
      yield received shouldBe Some(frame)

    "have no queue for an unknown player".in:
      for
        registry <- ConnectionRegistry()
        outbound <- registry.queueFor(PlayerId.random())
      yield outbound shouldBe None
