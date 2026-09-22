package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.geometry.Shape.AABB
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CollisionSystemSpec extends AnyFlatSpec with Matchers:

  private def spawnEntity(
      x: Double,
      y: Double,
      id: EntityId = EntityId.generate()
  ): (EntityId, List[Component]) =
    val position = PositionComponent(Point2D(x, y))
    val shape = ShapeComponent(AABB(2.0, 2.0, Point2D.origin))
    val movement = MovementComponent(Vector2D.zero)
    (id, List(position, shape, movement))

  "The CollisionSystem" should "emit a CollisionEvent when entities overlap" in:
    val id1 = EntityId.generate()
    val id2 = EntityId.generate()
    val entity1 = spawnEntity(-1.0, 0.0, id1)
    val entity2 = spawnEntity(1.0, 0.0, id2)
    val world = GameWorld(List(entity1, entity2))
    val (_, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }
    collisions should not be empty
    collisions should contain allOf (
      CollisionDetected(id1, id2),
      CollisionDetected(id2, id1)
    )

  it should "not emit any CollisionEvent when entities are distant and do not overlap" in:
    val entity1 = spawnEntity(-10.0, 0.0)
    val entity2 = spawnEntity(10.0, 0.0)
    val world = GameWorld(List(entity1, entity2))
    val (_, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }
    collisions shouldBe empty
