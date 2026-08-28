package com.unibo.scalaparty.infrastructure.network

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class ConnectionRegistrySpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "A ConnectionRegistry".should:

    "allow binding a session to a match and retrieving it".in:
      val playerId = PlayerId.random()
      val matchId  = MatchId.random()

      for
        registry <- ConnectionRegistry()
        _        <- registry.bindSessionToMatch(playerId, matchId)
        players  <- registry.getClientsForMatch(matchId)
      yield
        players.size shouldBe 1
        players.head shouldBe playerId

    "return an empty list for a match with no players".in:
      val matchId  = MatchId.random()

      for
        registry <- ConnectionRegistry()
        players  <- registry.getClientsForMatch(matchId)
      yield
        players shouldBe empty

    "remove a session correctly".in:
      val playerId = PlayerId.random()
      val matchId  = MatchId.random()

      for
        registry <- ConnectionRegistry()
        _        <- registry.bindSessionToMatch(playerId, matchId)
        _        <- registry.removeSession(playerId)
        players  <- registry.getClientsForMatch(matchId)
      yield
        players shouldBe empty

    "handle multiple players in the same match".in:
      val matchId  = MatchId.random()
      val player1  = PlayerId.random()
      val player2  = PlayerId.random()

      for
        registry <- ConnectionRegistry()
        _        <- registry.bindSessionToMatch(player1, matchId)
        _        <- registry.bindSessionToMatch(player2, matchId)
        players  <- registry.getClientsForMatch(matchId)
      yield
        players.size shouldBe 2
        players should contain allOf(player1, player2)