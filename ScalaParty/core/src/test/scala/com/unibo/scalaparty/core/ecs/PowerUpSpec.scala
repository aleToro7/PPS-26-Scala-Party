package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.ecs.Effect.{Boost, Repair}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PowerUpSpec extends AnyFlatSpec with Matchers:

  private val rapidFire = StatModifier(Stat.ShootCooldown, 0.5)

  "A PowerUp" should "reject a blank name" in:
    an[IllegalArgumentException] should be thrownBy PowerUp(" ", Repair(10.0))

  "A Boost" should "reject a non-positive duration" in:
    an[IllegalArgumentException] should be thrownBy Boost(rapidFire, 0L)

  "A Repair" should "reject a non-positive amount" in:
    an[IllegalArgumentException] should be thrownBy Repair(0.0)

  "A StatModifier" should "reject a non-positive factor" in:
    an[IllegalArgumentException] should be thrownBy StatModifier(Stat.BulletPower, 0.0)

  "An ActiveEffect" should "reject a non-positive remaining time" in:
    an[IllegalArgumentException] should be thrownBy ActiveEffect(rapidFire, 0L)

  it should "have less time left once some time has passed" in:
    ActiveEffect(rapidFire, 1000L).advanced(400L) shouldBe Some(ActiveEffect(rapidFire, 600L))

  it should "run out when its whole remaining time has passed" in:
    ActiveEffect(rapidFire, 1000L).advanced(1000L) shouldBe None
