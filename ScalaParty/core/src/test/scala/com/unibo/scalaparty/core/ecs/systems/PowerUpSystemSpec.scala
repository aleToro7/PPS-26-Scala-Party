package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.Effect.{Boost, Repair}
import com.unibo.scalaparty.core.ecs.PowerUpSpotComponent.State.{Available, Recharging}
import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.model.PowerUpSettings
import com.unibo.scalaparty.core.utils.PseudoRandom
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PowerUpSystemSpec extends AnyFlatSpec with Matchers with OptionValues:

  private val rapidFireModifier = StatModifier(Stat.ShootCooldown, 0.5)
  private val boostDuration = 5_000L
  private val rapidFire = PowerUp("rapid-fire", Boost(rapidFireModifier, boostDuration))
  private val repair = PowerUp("repair", Repair(30.0))
  private val respawnDelay = 10_000L
  private val pickupRadius = 25.0
  private val system = PowerUpSystem(PowerUpSettings(List(rapidFire), respawnDelay, pickupRadius))
  private val dt = 16L

  private val spotId = EntityId.generate()
  private val shipId = EntityId.generate()

  private def spotWith(state: PowerUpSpotComponent.State): EntityWithComponents =
    (spotId, List(PositionComponent(Point2D.origin), PowerUpSpotComponent(state, PseudoRandom(42L))))

  private def shipAt(position: Point2D, id: EntityId = shipId): EntityWithComponents =
    (id, List(PositionComponent(position), ActiveEffectsComponent(), HealthComponent(50.0, 100.0)))

  private val inRange = Point2D(pickupRadius - 1.0, 0.0)

  extension (world: GameWorld)
    private def effectsOf(id: EntityId): List[ActiveEffect] =
      world.findComponent[ActiveEffectsComponent](id).value.effects
    private def spot: PowerUpSpotComponent = world.findComponent[PowerUpSpotComponent](spotId).value

  "PowerUpSystem" should "hand the boost on a spot to a spaceship within the pickup radius" in:
    val world = GameWorld(List(spotWith(Available(rapidFire)), shipAt(inRange)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.effectsOf(shipId) shouldBe List(ActiveEffect(rapidFireModifier, boostDuration))

  it should "start recharging a spot once its power-up is picked up" in:
    val world = GameWorld(List(spotWith(Available(rapidFire)), shipAt(inRange)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.spot.state shouldBe Recharging(respawnDelay)

  it should "restore the health of a spaceship picking up a repair" in:
    val world = GameWorld(List(spotWith(Available(repair)), shipAt(inRange)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.findComponent[HealthComponent](shipId).value.current shouldBe 80.0

  it should "leave the power-up on its spot when no spaceship is within the pickup radius" in:
    val world = GameWorld(List(spotWith(Available(rapidFire)), shipAt(Point2D(pickupRadius + 1.0, 0.0))))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.spot.powerUp.value shouldBe rapidFire
    updatedWorld.effectsOf(shipId) shouldBe empty

  it should "not hand power-ups to entities unable to receive them" in:
    val bullet = (shipId, List(PositionComponent(inRange), BulletComponent(10.0, EntityId.generate())))
    val world = GameWorld(List(spotWith(Available(rapidFire)), bullet))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.spot.powerUp.value shouldBe rapidFire

  it should "hand the power-up to the closest spaceship within the pickup radius" in:
    val closerId = EntityId.generate()
    val world = GameWorld(List(spotWith(Available(rapidFire)), shipAt(inRange), shipAt(Point2D(1.0, 0.0), closerId)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.effectsOf(closerId) should have length 1
    updatedWorld.effectsOf(shipId) shouldBe empty

  it should "hand the power-up to the oldest spaceship among equally close ones" in:
    val newerId = EntityId.generate()
    val world =
      GameWorld(List(spotWith(Available(rapidFire)), shipAt(Point2D(0.0, 1.0), newerId), shipAt(Point2D(1.0, 0.0))))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.effectsOf(shipId) should have length 1
    updatedWorld.effectsOf(newerId) shouldBe empty

  it should "not hand anything from a recharging spot" in:
    val world = GameWorld(List(spotWith(Recharging(respawnDelay)), shipAt(inRange)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.spot.state shouldBe Recharging(respawnDelay - dt)
    updatedWorld.effectsOf(shipId) shouldBe empty

  it should "hand the power-up of a spot recharged in the same update" in:
    val world = GameWorld(List(spotWith(Recharging(dt)), shipAt(inRange)))

    val (updatedWorld, _) = system.update(world, Set.empty, dt)

    updatedWorld.effectsOf(shipId) shouldBe List(ActiveEffect(rapidFireModifier, boostDuration))
    updatedWorld.spot.state shouldBe Recharging(respawnDelay)

  it should "run down the effects in action" in:
    val ship = (shipId, List(ActiveEffectsComponent(List(ActiveEffect(rapidFireModifier, 1_000L)))))

    val (updatedWorld, _) = system.update(GameWorld(List(ship)), Set.empty, 1_000L)

    updatedWorld.effectsOf(shipId) shouldBe empty

  it should "forward the received events untouched" in:
    val events: Set[GameEvent] = Set(GameEvent.Death(shipId))

    val (_, updatedEvents) = system.update(GameWorld.empty, events, dt)

    updatedEvents shouldBe events
