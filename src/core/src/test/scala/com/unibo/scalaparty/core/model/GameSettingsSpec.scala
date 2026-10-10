package com.unibo.scalaparty.core.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameSettingsSpec extends AnyWordSpec with Matchers:

  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)

  "GameSettings" should:

    "initialize with default nested configurations" in:
      val settings = GameSettings.default

      settings.spaceship shouldBe SpaceshipSettings.default
      settings.spaceship.weapon shouldBe Weapon.default
      settings.matchSettings shouldBe MatchSettings.default

    "compose custom configurations accurately" in:
      val customWeapon = Weapon.default.copy(bulletPower = 20.0, bulletSpeed = 150.0, shootCooldown = 300L)
      val customSpaceship = SpaceshipSettings.default.copy(speed = 75.0, rotationSpeed = 90.0, weapon = customWeapon)
      val customMatch = MatchSettings(timeLimit = 30_000L)

      val custom = GameSettings.default.copy(spaceship = customSpaceship, matchSettings = customMatch)

      custom.spaceship shouldBe customSpaceship
      custom.spaceship.weapon shouldBe customWeapon
      custom.matchSettings shouldBe customMatch

  "SpaceshipSettings" should:

    "reject non-positive movement or rotation speeds" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings.default.copy(speed = invalid)
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings.default.copy(rotationSpeed = invalid)

    "reject non-positive max health" in:
      nonPositiveDoubles.foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy SpaceshipSettings.default.copy(maxHealth = invalid)

    "validate collision damage boundaries" in:
      an[IllegalArgumentException] should be thrownBy SpaceshipSettings.default.copy(collisionDamage = -1.0)
      noException should be thrownBy SpaceshipSettings.default.copy(collisionDamage = 0.0)

  "MatchSettings" should:

    "reject a non-positive time limit" in:
      List(0L, -1L).foreach: invalid =>
        an[IllegalArgumentException] should be thrownBy MatchSettings(timeLimit = invalid)
