package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{contain, should, shouldBe}

class EntityFactorySpec extends AnyFlatSpec:

  "EntityFactory" should "create a spaceship entity with the correct components" in:
    val position = Point2D(10, 20)
    val velocity = Vector2D(1, 1)
    val (entityId, components) =
      EntityFactory.createSpaceship(position, velocity)
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Spaceship)
    )

  "EntityFactory" should "create a bullet entity with the correct components" in:
    val shooterId = EntityId.generate()
    val position = Point2D(15, 25)
    val velocity = Vector2D(2, 0)
    val power = 12.5
    val bulletId = EntityId.generate()
    val (createdBulletId, components) =
      EntityFactory.createBullet(shooterId, position, velocity, power, bulletId)
    createdBulletId shouldBe bulletId
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Bullet),
      BulletComponent(power, shooterId)
    )
