package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.{CollisionDetected, Death}
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeathSystemSpec extends AnyFlatSpec with Matchers:

  private val defaultDt = 1_000L
  private val maxHealth = 100.0

  private val shipId = EntityId.generate()
  private val otherShipId = EntityId.generate()
  private val bulletId = EntityId.generate()

  private val world = GameWorld(
    List(
      EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, shipId, maxHealth = maxHealth),
      EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, otherShipId, maxHealth = maxHealth),
      EntityFactory.createBullet(shipId, Point2D.origin, Vector2D.zero, power = 10.0, bulletId)
    )
  )

  private def updateWorld(world: GameWorld, events: GameEvent*): (GameWorld, Set[GameEvent]) =
    DeathSystem.update(world, events.toSet, defaultDt)

  extension (world: GameWorld)
    private def withHealth(entityId: EntityId, health: Double): GameWorld =
      world.updateComponent(entityId, HealthComponent(health, maxHealth))

  "DeathSystem" should "not modify a world where every entity is alive" in:
    val damagedWorld = world.withHealth(shipId, 1.0)

    val (updatedWorld, events) = updateWorld(damagedWorld)

    events shouldBe empty
    updatedWorld shouldBe damagedWorld

  it should "remove the entities whose health is depleted" in:
    val (updatedWorld, _) = updateWorld(world.withHealth(shipId, 0.0))

    updatedWorld.entities should contain theSameElementsAs List(otherShipId, bulletId)

  it should "never remove the entities without health" in:
    val (updatedWorld, _) = updateWorld(world.withHealth(shipId, 0.0))

    updatedWorld.entities should contain(bulletId)

  it should "notify the death of every removed entity" in:
    val (_, events) = updateWorld(world.withHealth(shipId, 0.0).withHealth(otherShipId, 0.0))

    events shouldBe Set(Death(shipId), Death(otherShipId))

  it should "forward the received events" in:
    val collision = CollisionDetected(bulletId, shipId)

    val (_, events) = updateWorld(world.withHealth(shipId, 0.0), collision)

    events shouldBe Set(collision, Death(shipId))
