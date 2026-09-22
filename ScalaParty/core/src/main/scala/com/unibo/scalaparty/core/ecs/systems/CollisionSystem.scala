package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected
import com.unibo.scalaparty.core.geometry.{boundingBox, intersects, moveTo, Shape}
import com.unibo.scalaparty.core.utils.collectFirstOfClass

type CollisionPair = (EntityId, EntityId)

object CollisionPair:
  def apply(a: EntityId, b: EntityId): CollisionPair = if a.value < b.value then (a, b) else (b, a)

object CollisionSystem extends WorldSystem:
  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): (GameWorld, Set[GameEvent]) =
    val movingEntities = world.findEntitiesWithComponent[MovementComponent].map((id, _) => id).toSet
    val entityWithShapes = extractsShapes(world.findEntitiesWithComponent[ShapeComponent])
    val movingEntitiesWithShapes = entityWithShapes.filter((id, _) => movingEntities.contains(id))
    val collisions =
      for
        (actor, actorShape)   <- movingEntitiesWithShapes
        (target, targetShape) <- entityWithShapes
        actorAABB = actorShape.boundingBox
        targetAABB = targetShape.boundingBox
        // This line performs collision detection starting from the most lightweight check to end up with precise shape intersection check.
        if actor != target && actorAABB.intersects(targetAABB) && actorShape.intersects(targetShape)
      yield CollisionPair(actor, target)
    val collisionEvents = collisions.toSet.map(CollisionDetected.apply)
    (world, events ++ collisionEvents)

  private def extractsShapes(entities: Iterable[EntityWithComponents]): Iterable[(EntityId, Shape)] =
    entities.flatMap: (entityId, components) =>
      for
        ShapeComponent(shape)  <- components.collectFirstOfClass[ShapeComponent]
        PositionComponent(pos) <- components.collectFirstOfClass[PositionComponent]
      yield (entityId, shape moveTo pos)

private type EntityWithShape = (EntityId, Shape)

extension (e: EntityWithShape)
  def entityId: EntityId = e._1
  def shape: Shape = e._2
