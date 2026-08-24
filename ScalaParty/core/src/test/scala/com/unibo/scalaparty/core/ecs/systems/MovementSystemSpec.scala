package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MovementSystemSpec extends AnyFlatSpec with Matchers:

  private def createEntity(position: Point2D, velocity: Vector2D): EntityWithComponents =
    (EntityId.generate(), List(PositionComponent(position), MovementComponent(velocity)))

  "MovementSystem" should "not modify the world if there are no entities with movement components" in:
    val world = GameWorld(Nil)
    val dt = 1_000L // 1 second in milliseconds
    val (updatedWorld, events) = MovementSystem.update(world, Set.empty, dt)
    events shouldBe empty
    updatedWorld shouldBe world

  "MovementSystem" should "update the position of entities based on their velocity" in:
    val pos = Point2D.origin
    val vel = Vector2D(1.0, 1.0)
    val entity = createEntity(pos, vel)
    val world = GameWorld(List(entity))
    val dt = 1_000L // 1 second in milliseconds
    val dtInSeconds = dt.toDouble / 1_000.0
    val expectedPosition = pos + (vel * dtInSeconds)
    val (updatedWorld, events) = MovementSystem.update(world, Set.empty, dt)
    events shouldBe empty
    updatedWorld.id should not be world.id
    val actualPosition = updatedWorld
      .findComponents(entity._1)
      .get
      .collectFirst { case pc: PositionComponent => pc.position }
    actualPosition should not be empty
    actualPosition.get shouldBe expectedPosition
