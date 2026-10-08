package com.unibo.scalaparty.core.model

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WeaponSpec extends AnyFlatSpec with Matchers:

  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)

  "A Weapon" should "be valid with its default attributes" in:
    noException should be thrownBy Weapon()

  it should "reject non-positive bullet power" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy Weapon(bulletPower = invalid)

  it should "reject non-positive bullet speed" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy Weapon(bulletSpeed = invalid)

  it should "validate shoot cooldown boundaries" in:
    an[IllegalArgumentException] should be thrownBy Weapon(shootCooldown = -1L)
    noException should be thrownBy Weapon(shootCooldown = 0L)

  it should "validate muzzle offset boundaries" in:
    an[IllegalArgumentException] should be thrownBy Weapon(muzzleOffset = -1.0)
    noException should be thrownBy Weapon(muzzleOffset = 0.0)
