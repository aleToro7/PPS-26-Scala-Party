package com.unibo.scalaparty.core.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameSettingsSpec extends AnyWordSpec with Matchers:

  "GameSettings" should:

    "initialize with default unified parameters" in:
      val settings = GameSettings.default

      settings.worldWidth shouldEqual 800
      settings.worldHeight shouldEqual 800
      settings.spaceshipSpeed shouldEqual 1.0
      settings.spaceshipRotationSpeed shouldEqual 180.0

    "accept valid custom dimensions and speeds" in:
      val custom = GameSettings(
        worldWidth = 1920,
        worldHeight = 1080,
        spaceshipSpeed = 2.5,
        spaceshipRotationSpeed = 90.0
      )

      custom.worldWidth shouldEqual 1920
      custom.worldHeight shouldEqual 1080
      custom.spaceshipSpeed shouldEqual 2.5
      custom.spaceshipRotationSpeed shouldEqual 90.0

    "reject non-positive arena dimensions" in:
      an[IllegalArgumentException] should be thrownBy:
        GameSettings(worldWidth = 0, worldHeight = 600)

      an[IllegalArgumentException] should be thrownBy:
        GameSettings(worldWidth = 800, worldHeight = -100)

    "reject non-positive movement or rotation speeds" in:
      an[IllegalArgumentException] should be thrownBy:
        GameSettings(spaceshipSpeed = 0.0)

      an[IllegalArgumentException] should be thrownBy:
        GameSettings(spaceshipRotationSpeed = -45.0)