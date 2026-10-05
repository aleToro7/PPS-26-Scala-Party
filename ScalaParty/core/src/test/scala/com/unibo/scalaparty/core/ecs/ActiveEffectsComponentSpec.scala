package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.model.{Stat, StatModifier}
import com.unibo.scalaparty.core.model.Effect.Boost
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ActiveEffectsComponentSpec extends AnyFlatSpec with Matchers:

  private val rapidFire = StatModifier(Stat.ShootCooldown, 0.5)
  private val speedBoost = StatModifier(Stat.Speed, 1.5)

  "ActiveEffectsComponent" should "have no effects in action by default" in:
    ActiveEffectsComponent().effects shouldBe empty

  it should "leave every stat unmodified when no effects are in action" in:
    all(Stat.values.map(ActiveEffectsComponent().factorOf)) shouldBe 1.0

  it should "start a boost for its whole duration" in:
    ActiveEffectsComponent().activated(Boost(rapidFire, 5000L)).effects shouldBe List(ActiveEffect(rapidFire, 5000L))

  it should "modify only the stat targeted by a boost" in:
    val effects = ActiveEffectsComponent().activated(Boost(speedBoost, 5000L))
    effects.factorOf(Stat.Speed) shouldBe 1.5
    effects.factorOf(Stat.ShootCooldown) shouldBe 1.0

  it should "multiply the factors of the boosts on the same stat" in:
    val boost = Boost(speedBoost, 5000L)
    ActiveEffectsComponent().activated(boost).activated(boost).factorOf(Stat.Speed) shouldBe 2.25

  it should "drop only the effects that run out when time passes" in:
    val effects = ActiveEffectsComponent(List(ActiveEffect(rapidFire, 1000L), ActiveEffect(speedBoost, 3000L)))
    effects.advanced(1000L) shouldBe ActiveEffectsComponent(List(ActiveEffect(speedBoost, 2000L)))

  it should "leave a stat unmodified once its effects run out" in:
    val expired = ActiveEffectsComponent().activated(Boost(rapidFire, 1000L)).advanced(1000L)
    expired.factorOf(Stat.ShootCooldown) shouldBe 1.0

  it should "reject a negative time to advance by" in:
    an[IllegalArgumentException] should be thrownBy ActiveEffectsComponent().advanced(-1L)
