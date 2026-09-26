package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{boundingBox, moveTo, vertices, Point2D}
import com.unibo.scalaparty.core.geometry.Shape.AABB
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.core.utils.collectFirstOfClass

class ArenaSystem(private val settings: GameSettings) extends WorldSystem:
  private val arena = settings.arena
  private val maxArenaY = arena.height / 2.0
  private val minArenaY = -maxArenaY
  private val maxArenaX = arena.width / 2.0
  private val minArenaX = -maxArenaX

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val movingEntities = world.findEntitiesWithComponent[MovementComponent]
    val updatedWorld = movingEntities.foldLeft(world): (currentWorld, entity) =>
      val (entityId, components) = entity
      val newPosition =
        for
          position <- components.collectFirstOfClass[PositionComponent].map(_.position)
          shape    <- components.collectFirstOfClass[ShapeComponent].map(_.shape)
          boundingBox = shape.boundingBox.moveTo(position)
          if exceedsArena(boundingBox)
        yield repositionInsideArena(position, boundingBox)
      if newPosition.isDefined then currentWorld.updateComponent(entityId, PositionComponent(newPosition.get))
      else currentWorld
    (updatedWorld, events)

  private def exceedsArena(aabb: AABB): Boolean = aabb.vertices.exists: v =>
    v.x < minArenaX || v.x > maxArenaX || v.y < minArenaY || v.y > maxArenaY

  private def repositionInsideArena(position: Point2D, aabb: AABB): Point2D =
    val maxAssignableX = maxArenaX - aabb.width / 2.0
    val minAssignableX = minArenaX + aabb.width / 2.0
    val maxAssignableY = maxArenaY - aabb.height / 2.0
    val minAssignableY = minArenaY + aabb.height / 2.0
    val clampedX = position.x.max(minAssignableX).min(maxAssignableX)
    val clampedY = position.y.max(minAssignableY).min(maxAssignableY)
    Point2D(clampedX, clampedY)
