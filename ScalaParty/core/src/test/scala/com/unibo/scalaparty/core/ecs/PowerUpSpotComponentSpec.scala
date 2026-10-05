package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.ecs.PowerUpSpotComponent.State.{Available, Recharging}
import com.unibo.scalaparty.core.model.{PowerUp, Stat, StatModifier}
import com.unibo.scalaparty.core.model.Effect.{Boost, Repair}
import com.unibo.scalaparty.core.utils.PseudoRandom
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PowerUpSpotComponentSpec extends AnyFlatSpec with Matchers with OptionValues:

  private val repair = PowerUp("repair", Repair(30.0))
  private val rapidFire = PowerUp("rapid-fire", Boost(StatModifier(Stat.ShootCooldown, 0.5), 5_000L))
  private val catalog = List(repair, rapidFire)
  private val random = PseudoRandom(42L)
  private val respawnDelay = 10_000L

  "PowerUpSpotComponent" should "be stocked with a power-up drawn from the catalog" in:
    catalog should contain(PowerUpSpotComponent.stocked(catalog, random).powerUp.value)

  it should "draw the power-up it is stocked with deterministically" in:
    PowerUpSpotComponent.stocked(catalog, random) shouldBe PowerUpSpotComponent.stocked(catalog, random)

  it should "hold no power-up once emptied" in:
    PowerUpSpotComponent.stocked(catalog, random).emptied(respawnDelay).powerUp shouldBe None

  it should "reject a non-positive respawn delay" in:
    an[IllegalArgumentException] should be thrownBy PowerUpSpotComponent.stocked(catalog, random).emptied(0L)

  it should "keep recharging until its respawn delay has passed" in:
    val spot = PowerUpSpotComponent(Recharging(respawnDelay), random)
    spot.advanced(4_000L, catalog) shouldBe spot.copy(state = Recharging(6_000L))

  it should "be stocked again once its respawn delay has passed" in:
    val spot = PowerUpSpotComponent(Recharging(respawnDelay), random)
    spot.advanced(respawnDelay, catalog) shouldBe PowerUpSpotComponent.stocked(catalog, random)

  it should "keep its power-up while time passes" in:
    val spot = PowerUpSpotComponent(Available(repair), random)
    spot.advanced(respawnDelay, catalog) shouldBe spot

  it should "reject a negative time to advance by" in:
    an[IllegalArgumentException] should be thrownBy
      PowerUpSpotComponent.stocked(catalog, random).advanced(-1L, catalog)
