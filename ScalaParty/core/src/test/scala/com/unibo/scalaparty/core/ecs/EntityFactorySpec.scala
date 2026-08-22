package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.matchers.should.Matchers.contain

class EntityFactorySpec extends AnyFlatSpec:

  "EntityFactory" should "create a spaceship entity with the correct components" in:
    val position = Point2D(10, 20)
    val velocity = Vector2D(1, 1)
    val (entityId, components) = EntityFactory.createSpaceship(position, velocity)
    components should contain allOf(PositionComponent(position), MovementComponent(velocity))
