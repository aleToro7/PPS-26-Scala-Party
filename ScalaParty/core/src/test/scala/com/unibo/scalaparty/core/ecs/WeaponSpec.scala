package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.model.ShootingSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WeaponSpec extends AnyFlatSpec with Matchers:

  private val nonPositiveDoubles: List[Double] = List(0.0, -1.0)
  private val validPower = 10.0
  private val validSpeed = 100.0
  private val validCooldown = 250L
  private val validOffset = 12.0

  "A Weapon" should "instantiate correctly with valid attributes" in:
    val weapon = Weapon(
      bulletPower = validPower,
      bulletSpeed = validSpeed,
      shootCooldown = validCooldown,
      muzzleOffset = validOffset
    )

    weapon.bulletPower shouldBe validPower
    weapon.bulletSpeed shouldBe validSpeed
    weapon.shootCooldown shouldBe validCooldown
    weapon.muzzleOffset shouldBe validOffset

  it should "reject non-positive bullet power" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy:
        Weapon(
          bulletPower = invalid,
          bulletSpeed = validSpeed,
          shootCooldown = validCooldown,
          muzzleOffset = validOffset
        )

  it should "reject non-positive bullet speed" in:
    nonPositiveDoubles.foreach: invalid =>
      an[IllegalArgumentException] should be thrownBy:
        Weapon(
          bulletPower = validPower,
          bulletSpeed = invalid,
          shootCooldown = validCooldown,
          muzzleOffset = validOffset
        )

  it should "validate shoot cooldown boundaries" in:
    an[IllegalArgumentException] should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = -1L, muzzleOffset = validOffset)

    noException should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = 0L, muzzleOffset = validOffset)

  it should "validate muzzle offset boundaries" in:
    an[IllegalArgumentException] should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = validCooldown, muzzleOffset = -1.0)

    noException should be thrownBy:
      Weapon(bulletPower = validPower, bulletSpeed = validSpeed, shootCooldown = validCooldown, muzzleOffset = 0.0)

  it should "provide default attributes that satisfy all domain invariants" in:
    val defaultWeapon = Weapon.default

    defaultWeapon.bulletPower should be > 0.0
    defaultWeapon.bulletSpeed should be > 0.0
    defaultWeapon.shootCooldown should be >= 0L
    defaultWeapon.muzzleOffset should be >= 0.0

  it should "map fields accurately from ShootingSettings" in:
    val settings = ShootingSettings(bulletPower = 20.0, bulletSpeed = 150.0, shootCooldown = 500L, muzzleOffset = 8.0)
    val weapon = Weapon.fromSettings(settings)

    weapon.bulletPower shouldBe settings.bulletPower
    weapon.bulletSpeed shouldBe settings.bulletSpeed
    weapon.shootCooldown shouldBe settings.shootCooldown
    weapon.muzzleOffset shouldBe settings.muzzleOffset

  it should "support immutable copy operations" in:
    val base = Weapon(
      bulletPower = validPower,
      bulletSpeed = validSpeed,
      shootCooldown = validCooldown,
      muzzleOffset = validOffset
    )
    val buffed = base.copy(bulletSpeed = 200.0)

    buffed.bulletSpeed shouldBe 200.0
    buffed.bulletPower shouldBe base.bulletPower
    buffed.shootCooldown shouldBe base.shootCooldown
