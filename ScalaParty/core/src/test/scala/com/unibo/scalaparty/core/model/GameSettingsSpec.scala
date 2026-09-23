package com.unibo.scalaparty.core.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameSettingsSpec extends AnyWordSpec with Matchers:

  private val nonPositiveInts: List[Int]       = List(0, -1)
  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)

  "GameSettings" should:

    "initialize with default nested configurations" in:
      val settings = GameSettings.default

      settings.arena shouldBe ArenaSettings()
      settings.spaceship shouldBe SpaceshipSettings()
      settings.shooting shouldBe ShootingSettings()

    "compose custom configurations accurately" in:
      val customArena     = ArenaSettings(width = 1920, height = 1080)
      val customSpaceship = SpaceshipSettings(speed = 75.0, rotationSpeed = 90.0)
      val customShooting  = ShootingSettings(bulletPower = 20.0, bulletSpeed = 150.0, shootCooldown = 300L)

      val custom = GameSettings(customArena, customSpaceship, customShooting)

      custom.arena shouldBe customArena
      custom.spaceship shouldBe customSpaceship
      custom.shooting shouldBe customShooting

  "ArenaSettings" should:

    "reject non-positive dimensions" in:
      nonPositiveInts.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy ArenaSettings(width = invalid)
        an[IllegalArgumentException] should be thrownBy ArenaSettings(height = invalid)

  "SpaceshipSettings" should:

    "reject non-positive movement or rotation speeds" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings(speed = invalid)
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings(rotationSpeed = invalid)

  "ShootingSettings" should:

    "reject non-positive bullet power or speed" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy ShootingSettings(bulletPower = invalid)
        an[IllegalArgumentException] should be thrownBy ShootingSettings(bulletSpeed = invalid)

    "validate shoot cooldown boundaries" in:
      an[IllegalArgumentException] should be thrownBy ShootingSettings(shootCooldown = -1L)
      noException should be thrownBy ShootingSettings(shootCooldown = 0L)

    "validate muzzle offset boundaries" in:
      an[IllegalArgumentException] should be thrownBy ShootingSettings(muzzleOffset = -1.0)
      noException should be thrownBy ShootingSettings(muzzleOffset = 0.0)