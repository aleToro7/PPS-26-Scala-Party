package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConnectionRegistrySpec extends AnyWordSpec with Matchers {

  "A ConnectionRegistry" should {

    "allow binding a session to a match and retrieving it" in {
      val playerId = PlayerId.random()
      val matchId  = MatchId.random()

      val testProgram: IO[List[PlayerId]] = for {
        registry <- ConnectionRegistry.make()
        _ <- registry.bindSessionToMatch(playerId, matchId)
        players <- registry.getClientsForMatch(matchId)
      } yield players

      val result = testProgram.unsafeRunSync()

      result.size shouldBe 1
      result.head shouldBe playerId
    }

    "return an empty list for a match with no players" in {
      val matchId  = MatchId.random()

      val testProgram: IO[List[PlayerId]] = for {
        registry <- ConnectionRegistry.make()
        players  <- registry.getClientsForMatch(matchId)
      } yield players

      val result = testProgram.unsafeRunSync()

      result shouldBe empty
    }

    "remove a session correctly" in {
      val playerId = PlayerId.random()
      val matchId  = MatchId.random()

      val testProgram: IO[List[PlayerId]] = for {
        registry <- ConnectionRegistry.make()
        _ <- registry.bindSessionToMatch(playerId, matchId)
        _ <- registry.removeSession(playerId)
        players <- registry.getClientsForMatch(matchId)
      } yield players

      val result = testProgram.unsafeRunSync()

      result shouldBe empty
    }

    "handle multiple players in the same match" in {
      val matchId  = MatchId.random()
      val player1  = PlayerId.random()
      val player2  = PlayerId.random()

      val testProgram: IO[List[PlayerId]] = for {
        registry <- ConnectionRegistry.make()
        _ <- registry.bindSessionToMatch(player1, matchId)
        _ <- registry.bindSessionToMatch(player2, matchId)
        players <- registry.getClientsForMatch(matchId)
      } yield players

      val result = testProgram.unsafeRunSync()

      result.size shouldBe 2
      result should contain allOf (player1, player2)
    }
  }
}