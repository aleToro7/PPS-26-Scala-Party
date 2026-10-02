package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{contain, should, shouldBe}

class EntityFactorySpec extends AnyFlatSpec:

  "A Spaceship" should "be created with the correct components" in:
    val position = Point2D(10, 20)
    val velocity = Vector2D(1, 1)
    val (entityId, components) = EntityFactory.createSpaceship(position, velocity)
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Spaceship)
    )

  it should "be created at full health and able to deal collision damage" in:
    val maxHealth = 80.0
    val collisionDamage = 15.0
    val (_, components) = EntityFactory.createSpaceship(
      position = Point2D.origin,
      velocity = Vector2D.zero,
      maxHealth = maxHealth,
      collisionDamage = collisionDamage
    )
    components should contain allOf (
      HealthComponent.full(maxHealth),
      CollisionDamageComponent(collisionDamage)
    )

  "A Bullet" should "be created with the correct components" in:
    val shooterId = EntityId.generate()
    val position = Point2D(15, 25)
    val velocity = Vector2D(2, 0)
    val power = 12.5
    val bulletId = EntityId.generate()
    val (createdBulletId, components) = EntityFactory.createBullet(shooterId, position, velocity, power, bulletId)
    createdBulletId shouldBe bulletId
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Bullet),
      BulletComponent(power, shooterId)
    )

  "A Wall" should "be created with the correct components" in:
    val position = Point2D(5, 5)
    val width = 10.0
    val height = 2.0
    val (wallId, components) =
      EntityFactory.createWall(position, width, height)
    components should contain allOf (
      PositionComponent(position),
      ShapeComponent(Shape.AABB(width, height, Point2D.origin)),
      EntityTypeComponent(EntityType.Wall)
    )
