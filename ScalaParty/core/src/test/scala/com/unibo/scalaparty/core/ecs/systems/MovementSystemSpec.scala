package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MovementSystemSpec extends AnyFlatSpec with Matchers:

  private val OneSecondMillis: Long = 1_000L

  private def createMovingEntity(position: Point2D, velocity: Vector2D): EntityWithComponents =
    (EntityId.generate(), List(PositionComponent(position), MovementComponent(velocity)))

  private def createWorld(entities: EntityWithComponents*): GameWorld =
    GameWorld(entities.toList)

  private def updateWorld(world: GameWorld, dt: Long = OneSecondMillis): SystemOutput =
    MovementSystem.update(world, Set.empty, dt)

  extension (world: GameWorld)
    private def getPosition(entityId: EntityId): Option[Point2D] =
      world.findComponents(entityId).flatMap: components =>
        components.collectFirst { case PositionComponent(pos) => pos }

  "MovementSystem" should "not modify the world if there are no entities with movement components" in:
    val emptyWorld = createWorld()
    val (updatedWorld, events) = updateWorld(emptyWorld)
    events shouldBe empty
    updatedWorld shouldBe emptyWorld

  it should "update the position of entities based on their velocity" in:
    val startPos = Point2D.origin
    val velocity = Vector2D(1.0, 1.0)
    val entity @ (entityId, _) = createMovingEntity(startPos, velocity)
    val world = createWorld(entity)
    val expectedPos = startPos + (velocity * (OneSecondMillis.toDouble / 1_000.0))
    val (updatedWorld, events) = updateWorld(world)
    events shouldBe empty
    updatedWorld.id should not be world.id
    updatedWorld.getPosition(entityId) shouldBe Some(expectedPos)

  it should "not generate a new world if an entity does not actually move" in:
    val stationaryEntity = createMovingEntity(Point2D.origin, Vector2D.zero)
    val world = createWorld(stationaryEntity)
    val (updatedWorld, events) = updateWorld(world)
    events shouldBe empty
    updatedWorld.id shouldBe world.id
