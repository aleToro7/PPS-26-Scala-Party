package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.Point2D

/** A system responsible for updating the positions of entities in the game world based on their movement components and the elapsed time.
 */
object MovementSystem extends System:

  /** Updates the positions of entities in the game world based on their movement components and the elapsed time.
   *  @param world  the current state of the game world
   *  @param events a set of game events to be processed
   *  @param dt     the elapsed time since the last update, in milliseconds
   *   @return a tuple containing the updated game world and a set of new game events generated during the update
   */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): (GameWorld, Set[GameEvent]) =
    world.findEntitiesWithComponent[PositionComponent].foldLeft((world, events)):
      case ((currentWorld, _), (entityId, components)) =>
        val newPosition = updatePosition(entityId, components, dt)
        newPosition match
          case Some(pos) =>
            val updatedComponents = replacePositionComponent(components, pos)
            val newWorld = currentWorld + (entityId, updatedComponents)
            (newWorld, events)
          case None => (currentWorld, events)

  private def updatePosition(entityId: EntityId, components: List[Component], dt: Long): Option[Point2D] =
    val dtInSeconds = dt.toDouble / 1_000.0
    val position = components.collectFirst({ case pc: PositionComponent => pc.position })
    val velocity =
      components.collectFirst({ case mc: MovementComponent => mc.velocity })
    (position, velocity) match
      case (Some(position), Some(velocity)) => Some(position + (velocity * dtInSeconds))
      case _ => Option.empty

  private def replacePositionComponent(components: List[Component], newPosition: Point2D): List[Component] =
    components.map:
      case _: PositionComponent => PositionComponent(newPosition)
      case other => other
