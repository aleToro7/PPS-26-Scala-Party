package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class GameCommandServiceSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "GameCommandService".should:

    "successfully buffer a supported command for a specific match".in:
      val matchId = MatchId.random()
      val playerId = PlayerId.random()
      val command = IntentCommand.Rotate(90.0)

      for
        service <- GameCommandService()
        _       <- service.handleCommand(matchId, playerId, command)
      yield succeed

    "gracefully ignore unsupported commands without failing".in:
      val matchId = MatchId.random()
      val playerId = PlayerId.random()
      val command = IntentCommand.Shoot

      for
        service <- GameCommandService()
        _       <- service.handleCommand(matchId, playerId, command)
      yield succeed
