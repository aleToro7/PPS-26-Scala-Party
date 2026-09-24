package com.unibo.scalaparty.core.ecs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HealthComponentSpec extends AnyFlatSpec with Matchers:

  private val maxHealth = 100.0

  "HealthComponent" should "start at full health when created with full" in:
    HealthComponent.full(maxHealth) shouldBe HealthComponent(maxHealth, maxHealth)

  it should "reduce the current health by the damage amount" in:
    HealthComponent.full(maxHealth).damaged(30.0).current shouldBe 70.0

  it should "never drop below zero health" in:
    HealthComponent.full(maxHealth).damaged(maxHealth * 2).current shouldBe 0.0

  it should "reject a negative damage" in:
    an[IllegalArgumentException] should be thrownBy HealthComponent.full(maxHealth).damaged(-1.0)

  it should "reject a non-positive max health" in:
    an[IllegalArgumentException] should be thrownBy HealthComponent.full(0.0)

  it should "reject a current health outside the valid range" in:
    an[IllegalArgumentException] should be thrownBy HealthComponent(-1.0, maxHealth)
    an[IllegalArgumentException] should be thrownBy HealthComponent(maxHealth + 1, maxHealth)
