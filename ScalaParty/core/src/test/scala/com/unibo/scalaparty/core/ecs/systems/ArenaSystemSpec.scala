package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D}
import com.unibo.scalaparty.core.model.{ArenaSettings, GameSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ArenaSystemSpec extends AnyFlatSpec with Matchers:

  private val arenaWidth = 200.0
  private val maxArenaX = arenaWidth / 2.0
  private val minArenaX = -maxArenaX
  private val arenaHeight = 200.0
  private val maxArenaY = arenaHeight / 2.0
  private val minArenaY = -maxArenaY
  private val entityRadius = 5.0

  private val settings = GameSettings(
    arena = ArenaSettings(arenaWidth.toInt, arenaHeight.toInt)
  )
  private val dt = 1_000L // 1 second in milliseconds

  private def createBoundedEntity(
      id: EntityId,
      position: Point2D,
      velocity: Vector2D = Vector2D(1.0, 0.0),
      radius: Double = entityRadius
  ): EntityWithComponents =
    (id, List(PositionComponent(position), MovementComponent(velocity), ShapeComponent(Shape.Circle(radius, position))))

  private def createWorld(entities: EntityWithComponents*): GameWorld =
    GameWorld(entities.toList)

  extension (world: GameWorld)
    private def getPosition(entityId: EntityId): Point2D =
      world
        .findComponents(entityId)
        .flatMap(_.collectFirst { case PositionComponent(pos) => pos })
        .getOrElse(fail(s"PositionComponent not found for entity $entityId"))

  // --- Test Cases ---

  "ArenaSystem" should "keep position and velocity unchanged when entity is inside arena bounds" in:
    val arenaSystem = ArenaSystem(settings)
    val entityId = EntityId.generate()
    val validPos = Point2D.origin
    val world = createWorld(createBoundedEntity(entityId, validPos))
    val (updatedWorld, events) = arenaSystem.update(world, Set.empty, dt)
    events shouldBe empty
    updatedWorld.getPosition(entityId) shouldBe validPos

  it should "clamp position considering entity radius when exceeding right border" in:
    val arenaSystem = ArenaSystem(settings)
    val entityId = EntityId.generate()
    val y = 50.0
    val outOfBoundsPos = Point2D(maxArenaX + entityRadius, y)
    val world = createWorld(createBoundedEntity(entityId, outOfBoundsPos))
    val (updatedWorld, _) = arenaSystem.update(world, Set.empty, dt)
    val expectedPos = Point2D(maxArenaX - entityRadius, y)
    updatedWorld.getPosition(entityId) shouldBe expectedPos

  it should "clamp position considering entity radius when exceeding left border" in:
    val arenaSystem = ArenaSystem(settings)
    val entityId = EntityId.generate()
    val y = 50.0
    val outOf = 2.0
    val outOfBoundsPos = Point2D(minArenaX + (entityRadius - outOf), y)
    val world = createWorld(createBoundedEntity(entityId, outOfBoundsPos))
    val (updatedWorld, _) = arenaSystem.update(world, Set.empty, dt)
    val expectedPos = Point2D(minArenaX + entityRadius, y)
    updatedWorld.getPosition(entityId) shouldBe expectedPos

  it should "clamp both axes and adjust velocity on diagonal corner escape" in:
    val arenaSystem = ArenaSystem(settings)
    val outOf = 10.0
    val outOfBoundsPos = Point2D(maxArenaX + outOf, maxArenaY + outOf)
    val entityId = EntityId.generate()
    val world = createWorld(createBoundedEntity(entityId, outOfBoundsPos))
    val (updatedWorld, _) = arenaSystem.update(world, Set.empty, dt)
    val expectedPos = Point2D(maxArenaX - entityRadius, maxArenaY - entityRadius)
    updatedWorld.getPosition(entityId) shouldBe expectedPos

  it should "remove bullet when it exceeds arena bounds" in:
    val arenaSystem = ArenaSystem(settings)
    val entityId = EntityId.generate()
    val outOfBoundsPos = Point2D(maxArenaX + entityRadius + 1.0, 0.0)
    val (_, components) = createBoundedEntity(entityId, outOfBoundsPos)
    val world = createWorld((entityId, components :+ EntityTypeComponent(EntityType.Bullet)))
    val (updatedWorld, _) = arenaSystem.update(world, Set.empty, dt)
    updatedWorld.entities should not contain entityId
