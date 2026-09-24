package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.dto.EntityDto.Spaceship
import com.unibo.scalaparty.core.dto.toDto
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.geometry.Shape.AABB
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EntityAdapterSpec extends AnyFlatSpec with Matchers:

  "EntityAdapter" should "convert a spaceship entity to its corresponding DTO" in:
    val entityId = EntityId.generate()
    val position = Point2D(10, 20)
    val velocity = Vector2D(1, 1)
    val shape = AABB(2, 2, Point2D.origin)
    val components =
      List(
        PositionComponent(position),
        MovementComponent(velocity),
        EntityTypeComponent(EntityType.Spaceship),
        ShapeComponent(shape)
      )
    val entity = (entityId, components)
    val dto = entity.toDto
    dto should not be None
    dto.get shouldBe a[Spaceship]

  it should "return None for a spaceship entity missing required components" in:
    val entityId = EntityId.generate()
    val components = List(EntityTypeComponent(EntityType.Spaceship))
    val entity = (entityId, components)
    val dto = entity.toDto
    dto shouldBe None

  it should "return None for an entity with an unknown type" in:
    val entityId = EntityId.generate()
    val components = List() // No EntityTypeComponent
    val entity = (entityId, components)
    val dto = entity.toDto
    dto shouldBe None
