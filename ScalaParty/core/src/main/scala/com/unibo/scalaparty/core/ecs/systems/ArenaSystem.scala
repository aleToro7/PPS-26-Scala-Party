package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{rotate as rotatePolygon, *}
import com.unibo.scalaparty.core.geometry.Shape.AABB
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.core.utils.{collectFirstOfClass, half}

/** A system that ensures entities remain within the defined arena boundaries.
 *  It checks the position of entities with a `MovementComponent` and adjusts their position if they exceed the arena limits, taking into account their bounding box.
 *  @param settings the game settings containing arena dimensions
 */
class ArenaSystem(private val settings: GameSettings) extends WorldSystem:
  private val arena = settings.map.dimension
  private val maxArenaY = arena.height.half
  private val minArenaY = -maxArenaY
  private val maxArenaX = arena.width.half
  private val minArenaX = -maxArenaX

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val movingEntities = world.findEntitiesWithComponent[MovementComponent]
    val updatedWorld = movingEntities.foldLeft(world): (currentWorld, entity) =>
      val (entityId, components) = entity
      val newPosition = getNewPositionIfOutsideArena(components)
      val entityType = components.collectFirstOfClass[EntityTypeComponent].map(_.entityType)
      newPosition.fold(currentWorld): pos =>
        entityType match
          // If the entity is a bullet and exceeds the arena, remove it from the world
          case Some(EntityType.Bullet) => currentWorld.removeEntity(entityId)
          // If the entity is not a bullet, update its position to be inside the arena
          case _ => currentWorld.updateComponent(entityId, PositionComponent(pos))
    (updatedWorld, events)

  private def getNewPositionIfOutsideArena(components: Iterable[Component]) =
    for
      position <- components.collectFirstOfClass[PositionComponent].map(_.position)
      shape    <- components.collectFirstOfClass[ShapeComponent].map(_.shape)
      rotation = components.collectFirstOfClass[RotationComponent].map(_.angle) getOrElse 0.0
      boundingBox = shape.moveTo(position).rotate(rotation).boundingBox
      if exceedsArena(boundingBox)
    yield repositionInsideArena(position, boundingBox)

  private def exceedsArena(aabb: AABB): Boolean = aabb.vertices.exists: v =>
    v.x < minArenaX || v.x > maxArenaX || v.y < minArenaY || v.y > maxArenaY

  private def repositionInsideArena(position: Point2D, aabb: AABB): Point2D =
    val halfWidth = aabb.width.half
    val halfHeight = aabb.height.half
    val maxAssignableX = maxArenaX - halfWidth
    val minAssignableX = minArenaX + halfWidth
    val maxAssignableY = maxArenaY - halfHeight
    val minAssignableY = minArenaY + halfHeight
    val clampedX = position.x.max(minAssignableX).min(maxAssignableX)
    val clampedY = position.y.max(minAssignableY).min(maxAssignableY)
    Point2D(clampedX, clampedY)

  extension (shape: Shape)
    private def rotate(angle: Double): Shape = shape match
      case p: Shape.Polygon => p.rotatePolygon(angle)
      case _ => shape // For Circle and AABB, rotation does not change the shape
