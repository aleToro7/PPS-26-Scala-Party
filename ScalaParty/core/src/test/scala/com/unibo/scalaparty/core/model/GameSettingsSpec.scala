package com.unibo.scalaparty.core.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameSettingsSpec extends AnyWordSpec with Matchers:
  "GameSettings" should:
    "initialize with default unified parameters" in:
      val settings = GameSettings.default
      settings.worldWidth shouldEqual 800
      settings.spaceshipSpeed shouldEqual 1.0