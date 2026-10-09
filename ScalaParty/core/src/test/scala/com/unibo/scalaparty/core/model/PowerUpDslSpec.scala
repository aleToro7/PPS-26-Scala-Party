package com.unibo.scalaparty.core.model

import scala.concurrent.duration.DurationInt

import com.unibo.scalaparty.core.model.PowerUpDsl.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PowerUpDslSpec extends AnyFlatSpec with Matchers:

  "PowerUpDsl" should "declare a power-up scaling a stat for some time" in:
    powerUp("rapid-fire") lasting 8.seconds scaling ShootCooldown by 0.5 shouldBe
      PowerUp("rapid-fire", Effect.Boost(StatModifier(Stat.ShootCooldown, 0.5), 8_000L))

  it should "declare a power-up instantly restoring health" in:
    powerUp("repair") healing 30 shouldBe PowerUp("repair", Effect.Repair(30.0))

  it should "convert the duration of a boost to milliseconds" in:
    powerUp("shield") lasting 1500.millis scaling DamageTaken by 0.25 shouldBe
      PowerUp("shield", Effect.Boost(StatModifier(Stat.DamageTaken, 0.25), 1_500L))

  it should "not yield a power-up from a declaration left halfway" in:
    """val p: PowerUp = powerUp("damage") lasting 8.seconds""" shouldNot typeCheck
    """val p: PowerUp = powerUp("damage") lasting 8.seconds scaling BulletPower""" shouldNot typeCheck

  it should "still validate the declared values" in:
    an[IllegalArgumentException] should be thrownBy (powerUp("damage") lasting 8.seconds scaling BulletPower by 0.0)
    an[IllegalArgumentException] should be thrownBy (powerUp(" ") healing 30)
