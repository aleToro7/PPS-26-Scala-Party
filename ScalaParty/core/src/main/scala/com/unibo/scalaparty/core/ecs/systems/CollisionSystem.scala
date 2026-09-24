package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected
import com.unibo.scalaparty.core.geometry.*
import com.unibo.scalaparty.core.utils.collectFirstOfClass

type CollisionPair = (EntityId, EntityId)

object CollisionPair:
  def apply(a: EntityId, b: EntityId): CollisionPair = if a.value < b.value then (a, b) else (b, a)

object CollisionSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): (GameWorld, Set[GameEvent]) =
    val movingEntities = world.findEntitiesWithComponent[MovementComponent].map((id, _) => id).toSet
    val entityWithShapes = extractShapes(world)
    val movingEntitiesWithShapes = entityWithShapes.filter((id, _) => movingEntities.contains(id))
    val detectedCollisions = detectCollisions(movingEntitiesWithShapes, entityWithShapes)
    val uniqueCollisions = detectedCollisions
      .groupBy((actor, target, _) => CollisionPair(actor, target))
      .values
      .flatMap(_.headOption) // Keep only one collision per unique pair of entities
    // Apply MTV position resolution immutably to the GameWorld
    val resolvedWorld = uniqueCollisions.foldLeft(world): (currentWorld, collision) =>
      resolveCollision(currentWorld, collision, movingEntities)
    val collisionEvents = uniqueCollisions.map((actor, target, _) => CollisionDetected(actor, target)).toSet
    (resolvedWorld, events ++ collisionEvents)

  private def detectCollisions(
      movingEntitiesWithShapes: Iterable[EntityWithShape],
      entitiesWithShapes: Iterable[EntityWithShape]
  ): Iterable[(EntityId, EntityId, Vector2D)] =
    for
      (actor, actorShape)   <- movingEntitiesWithShapes
      (target, targetShape) <- entitiesWithShapes
      if actor != target
      if actorShape.boundingBox intersects targetShape.boundingBox
      mtv <- actorShape.penetratingVector(targetShape)
    yield (actor, target, mtv)

  private def resolveCollision(
      world: GameWorld,
      collision: (EntityId, EntityId, Vector2D),
      movingEntities: Set[EntityId]
  ): GameWorld =
    val (actor, target, mtv) = collision
    val isTargetMoving = movingEntities.contains(target)
    val slop = 0.001
    val bufferedMtv = mtv + (mtv.normalized * slop) // This small buffer prevents jittering when entities are in contact
    if isTargetMoving then
      // Dynamic vs Dynamic collision: push actor in negative direction (-50%) and target in positive direction (+50%)
      val halfMtv = bufferedMtv * 0.5
      val worldAfterActor = moveEntity(world, actor, -halfMtv)
      moveEntity(worldAfterActor, target, halfMtv)
    else
      // Dynamic vs Static collision: actor absorbs 100% of the displacement in negative direction
      moveEntity(world, actor, -bufferedMtv)

  private def moveEntity(world: GameWorld, entityId: EntityId, displacement: Vector2D): GameWorld =
    world.findComponent[PositionComponent](entityId) match
      case Some(PositionComponent(pos)) => world.updateComponent(entityId, PositionComponent(pos + displacement))
      case _ => world

  /** Extracts and transform the shapes of all entities in the world, moving them to their current positions.
   * @param world the game world containing entities and their components
   * @return an iterable of entities with their shapes transformed to their current positions
   */
  private def extractShapes(world: GameWorld): Iterable[EntityWithShape] =
    for
      (entityId, components) <- world.findEntitiesWithComponent[ShapeComponent]
      shape                  <- components.collectFirstOfClass[ShapeComponent].map(_.shape)
      PositionComponent(pos) <- components.collectFirstOfClass[PositionComponent]
    yield (entityId, shape moveTo pos)

private type EntityWithShape = (EntityId, Shape)

extension (e: EntityWithShape)
  def entityId: EntityId = e._1
  def shape: Shape = e._2
