package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.model.{Stat, StatModifier}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActiveEffectSpec extends AnyFlatSpec with Matchers:

  private val rapidFire = StatModifier(Stat.ShootCooldown, 0.5)

  "An ActiveEffect" should "reject a non-positive remaining time" in:
    an[IllegalArgumentException] should be thrownBy ActiveEffect(rapidFire, 0L)

  it should "have less time left once some time has passed" in:
    ActiveEffect(rapidFire, 1000L).advanced(400L) shouldBe Some(ActiveEffect(rapidFire, 600L))

  it should "run out when its whole remaining time has passed" in:
    ActiveEffect(rapidFire, 1000L).advanced(1000L) shouldBe None
