package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

object EntityFactory:

  /** Creates a new spaceship entity with the specified position and velocity.
   *
   * @param position the initial position of the spaceship
   * @param velocity the initial velocity of the spaceship
   * @return a tuple containing the unique identifier of the created spaceship entity and its associated list of components
   */
  def createSpaceship(
     position: Point2D,
     velocity: Vector2D,
     entityId: EntityId = EntityId.generate()
   ): (EntityId, List[Component]) =
    val components: List[Component] = List(
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Spaceship)
    )
    (entityId, components)
