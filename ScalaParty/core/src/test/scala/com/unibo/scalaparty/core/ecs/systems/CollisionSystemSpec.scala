package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected
import com.unibo.scalaparty.core.geometry.{center, Point2D, Shape, Vector2D}
import com.unibo.scalaparty.core.geometry.Shape.{AABB, Polygon}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CollisionSystemSpec extends AnyFlatSpec with Matchers:

  private def spawnEntity(
      x: Double,
      y: Double,
      shape: Shape = AABB(2.0, 2.0, Point2D.origin),
      id: EntityId = EntityId.generate()
  ): (EntityId, List[Component]) =
    val position = PositionComponent(Point2D(x, y))
    val shapeComponent = ShapeComponent(shape)
    val movement = MovementComponent(Vector2D.zero)
    (id, List(position, shapeComponent, movement))

  "The CollisionSystem" should "emit a CollisionEvent when entities overlap" in:
    val id1 = EntityId.generate()
    val id2 = EntityId.generate()
    val entity1 = spawnEntity(-1.0, 0.0, id = id1)
    val entity2 = spawnEntity(0.0, 0.0, id = id2)
    val world = GameWorld(List(entity1, entity2))
    val (_, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }

    collisions should not be empty
    collisions should contain oneOf (
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

  it should "emit exactly one CollisionEvent for a pair of overlapping entities, ignoring order" in:
    val id1 = EntityId.generate()
    val id2 = EntityId.generate()
    val entity1 = spawnEntity(0.0, 0.0, id = id1)
    val entity2 = spawnEntity(0.0, 0.0, id = id2)
    val world = GameWorld(List(entity1, entity2))
    val (_, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }
    collisions should have size 1

  it should "not emit a CollisionEvent when bounding boxes overlap but exact polygons do not intersect" in:
    val triangle1: Polygon = Polygon(
      Point2D(0.0, 0.0),
      Point2D(2.0, 0.0),
      Point2D(0.0, 2.0)
    )
    val triangle2: Polygon = Polygon(
      Point2D(1.0, 1.5),
      Point2D(3.0, 1.5),
      Point2D(3.0, 3.0)
    )
    val center1 = triangle1.center
    val center2 = triangle2.center
    val entity1 = spawnEntity(center1.x, center1.y, shape = triangle1)
    val entity2 = spawnEntity(center2.x, center2.y, shape = triangle2)
    val world = GameWorld(List(entity1, entity2))
    val (_, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }
    collisions shouldBe empty

  it should "resolve collisions between entities correctly and maintain world immutability invariants" in:
    val id1 = EntityId.generate()
    val id2 = EntityId.generate()
    val entity1 = spawnEntity(-1.0, 0.0, id = id1)
    val entity2 = spawnEntity(0.0, 0.0, id = id2)
    val world = GameWorld(List(entity1, entity2))
    // Tick 1: Collision detected, position resolved, new world snapshot generated with a new ID
    val (updatedWorld, events) = CollisionSystem.update(world, Set.empty, 1000)
    val collisions = events.collect { case c: CollisionDetected => c }
    collisions should have size 1
    world.id should not equal updatedWorld.id
    // Tick 2: Entities are separated, no new collision detected, world instance remains unchanged
    val (sameWorld, newEvents) = CollisionSystem.update(updatedWorld, Set.empty, 1000)
    newEvents shouldBe empty
    sameWorld.id shouldBe updatedWorld.id
