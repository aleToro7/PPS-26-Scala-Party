package com.unibo.scalaparty.core.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameSettingsSpec extends AnyWordSpec with Matchers:

  private val nonPositiveInts: List[Int] = List(0, -1)
  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)

  "GameSettings" should:

    "initialize with default nested configurations" in:
      val settings = GameSettings.default

      settings.spaceship shouldBe SpaceshipSettings()
      settings.shooting shouldBe ShootingSettings()
      settings.matchSettings shouldBe MatchSettings()

    "compose custom configurations accurately" in:
      val customSpaceship = SpaceshipSettings(speed = 75.0, rotationSpeed = 90.0)
      val customShooting = ShootingSettings(bulletPower = 20.0, bulletSpeed = 150.0, shootCooldown = 300L)
      val customMatch = MatchSettings(timeLimit = 30_000L)

      val custom = GameSettings(customSpaceship, customShooting, matchSettings = customMatch)

      custom.spaceship shouldBe customSpaceship
      custom.shooting shouldBe customShooting
      custom.matchSettings shouldBe customMatch

  "SpaceshipSettings" should:

    "reject non-positive movement or rotation speeds" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings(speed = invalid)
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings(rotationSpeed = invalid)

    "reject non-positive max health" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings(maxHealth = invalid)

    "validate collision damage boundaries" in:
      an[IllegalArgumentException] should be thrownBy SpaceshipSettings(collisionDamage = -1.0)
      noException should be thrownBy SpaceshipSettings(collisionDamage = 0.0)

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

  "MatchSettings" should:

    "reject a non-positive time limit" in:
      List(0L, -1L).foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy MatchSettings(timeLimit = invalid)

  "PowerUpSettings" should:

    "be part of the default game settings" in:
      GameSettings.default.powerUps shouldBe PowerUpSettings()

    "reject an empty catalog" in:
      an[IllegalArgumentException] should be thrownBy PowerUpSettings(catalog = Nil)

    "reject a non-positive respawn delay" in:
      List(0L, -1L).foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy PowerUpSettings(respawnDelay = invalid)

    "reject a non-positive pickup radius" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy PowerUpSettings(pickupRadius = invalid)
