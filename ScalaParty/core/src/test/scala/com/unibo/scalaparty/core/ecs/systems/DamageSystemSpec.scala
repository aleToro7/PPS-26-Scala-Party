package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DamageSystemSpec extends AnyFlatSpec with Matchers with OptionValues:

  private val defaultDt = 1_000L
  private val maxHealth = 100.0
  private val collisionDamage = 20.0
  private val bulletPower = 10.0

  private val shipId = EntityId.generate()
  private val otherShipId = EntityId.generate()
  private val bulletId = EntityId.generate()

  private def ship(entityId: EntityId): EntityWithComponents =
    EntityFactory.createSpaceship(
      position = Point2D.origin,
      velocity = Vector2D.zero,
      entityId = entityId,
      maxHealth = maxHealth,
      collisionDamage = collisionDamage
    )

  private def bulletShotBy(shooterId: EntityId, entityId: EntityId = bulletId): EntityWithComponents =
    EntityFactory.createBullet(shooterId, Point2D.origin, Vector2D.zero, bulletPower, entityId)

  private def updateWorld(world: GameWorld, events: GameEvent*): (GameWorld, Set[GameEvent]) =
    DamageSystem.update(world, events.toSet, defaultDt)

  extension (world: GameWorld)
    private def healthOf(entityId: EntityId): Double =
      world
        .findComponents(entityId)
        .flatMap(_.collectFirst { case hc: HealthComponent => hc.current })
        .value

  "DamageSystem" should "not modify the world if there are no collisions" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(otherShipId)))

    val (updatedWorld, events) = updateWorld(world)

    events shouldBe empty
    updatedWorld shouldBe world

  it should "damage a spaceship hit by a bullet by the bullet power" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(otherShipId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(bulletId, shipId))

    updatedWorld.healthOf(shipId) shouldBe maxHealth - bulletPower

  it should "consume a bullet once it damages an entity" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(otherShipId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(bulletId, shipId))

    updatedWorld.entities should not contain bulletId

  it should "resolve a collision regardless of the order of the involved entities" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(otherShipId)))

    val (bulletFirst, _) = updateWorld(world, CollisionDetected(bulletId, shipId))
    val (shipFirst, _) = updateWorld(world, CollisionDetected(shipId, bulletId))

    shipFirst.entitiesWithComponents should contain theSameElementsAs bulletFirst.entitiesWithComponents

  it should "not let a bullet damage its own shooter" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(shipId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(bulletId, shipId))

    updatedWorld.healthOf(shipId) shouldBe maxHealth
    updatedWorld.entities should contain(bulletId)

  it should "damage both spaceships involved in a collision by each other's collision damage" in:
    val world = GameWorld(List(ship(shipId), ship(otherShipId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(shipId, otherShipId))

    updatedWorld.healthOf(shipId) shouldBe maxHealth - collisionDamage
    updatedWorld.healthOf(otherShipId) shouldBe maxHealth - collisionDamage

  it should "not modify entities without health" in:
    val otherBulletId = EntityId.generate()
    val world = GameWorld(List(bulletShotBy(shipId), bulletShotBy(otherShipId, otherBulletId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(bulletId, otherBulletId))

    updatedWorld shouldBe world

  it should "ignore collisions involving entities no longer in the world" in:
    val world = GameWorld(List(ship(shipId)))

    val (updatedWorld, _) = updateWorld(world, CollisionDetected(bulletId, shipId))

    updatedWorld shouldBe world

  it should "forward the received events" in:
    val world = GameWorld(List(ship(shipId), bulletShotBy(otherShipId)))
    val collision = CollisionDetected(bulletId, shipId)

    val (_, events) = updateWorld(world, collision)

    events shouldBe Set(collision)
