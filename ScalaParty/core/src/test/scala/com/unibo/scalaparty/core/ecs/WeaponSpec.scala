package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.model.ShootingSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WeaponSpec extends AnyFlatSpec with Matchers:

  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)
  private val validPower                       = 10.0
  private val validSpeed                       = 100.0
  private val validCooldown                    = 250L

  "A Weapon" should "instantiate correctly with valid attributes" in:
    val weapon = Weapon(
      bulletPower = validPower,
      bulletSpeed = validSpeed,
      shootCooldown = validCooldown
    )

    weapon.bulletPower shouldBe validPower
    weapon.bulletSpeed shouldBe validSpeed
    weapon.shootCooldown shouldBe validCooldown

  it should "reject non-positive bullet power" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy:
        Weapon(bulletPower = invalid, bulletSpeed = validSpeed, shootCooldown = validCooldown)

  it should "reject non-positive bullet speed" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy:
        Weapon(bulletPower = validPower, bulletSpeed = invalid, shootCooldown = validCooldown)

  it should "validate shoot cooldown boundaries" in:
    an[IllegalArgumentException] should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = -1L)

    noException should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = 0L)

  it should "provide default attributes that satisfy all domain invariants" in:
    val defaultWeapon = Weapon.default()

    defaultWeapon.bulletPower should be > 0.0
    defaultWeapon.bulletSpeed should be > 0.0
    defaultWeapon.shootCooldown should be >= 0L

  it should "support immutable copy operations" in:
    val base = Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = validCooldown)
    val buffed = base.copy(bulletSpeed = 200.0)

    buffed.bulletSpeed shouldBe 200.0
    buffed.bulletPower shouldBe base.bulletPower
    buffed.shootCooldown shouldBe base.shootCooldown