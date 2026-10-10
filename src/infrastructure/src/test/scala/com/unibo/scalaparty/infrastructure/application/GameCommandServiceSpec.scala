package com.unibo.scalaparty.infrastructure.application

import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class GameCommandServiceSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "GameCommandService".should:

    "return the buffered commands of a match in arrival order".in:
      val matchId = MatchId.random()
      val first = PlayerId.random()
      val second = PlayerId.random()

      for
        service <- GameCommandService()
        _       <- service.handleCommand(matchId, first, PlayerInput.Rotate(90.0))
        _       <- service.handleCommand(matchId, second, PlayerInput.Shoot)
        drained <- service.drainCommands(matchId)
      yield drained shouldEqual List(first -> PlayerInput.Rotate(90.0), second -> PlayerInput.Shoot)

    "clear the buffer of a match once its commands are drained".in:
      val matchId = MatchId.random()

      for
        service <- GameCommandService()
        _       <- service.handleCommand(matchId, PlayerId.random(), PlayerInput.Shoot)
        _       <- service.drainCommands(matchId)
        drained <- service.drainCommands(matchId)
      yield drained shouldBe empty

    "keep the commands of different matches separate".in:
      val matchA = MatchId.random()
      val matchB = MatchId.random()
      val playerA = PlayerId.random()
      val playerB = PlayerId.random()

      for
        service  <- GameCommandService()
        _        <- service.handleCommand(matchA, playerA, PlayerInput.Rotate(45.0))
        _        <- service.handleCommand(matchB, playerB, PlayerInput.Shoot)
        drainedA <- service.drainCommands(matchA)
        drainedB <- service.drainCommands(matchB)
      yield
        drainedA shouldEqual List(playerA -> PlayerInput.Rotate(45.0))
        drainedB shouldEqual List(playerB -> PlayerInput.Shoot)
