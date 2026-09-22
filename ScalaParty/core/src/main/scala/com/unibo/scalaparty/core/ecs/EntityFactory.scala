package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

object EntityFactory:

  /** Creates a new spaceship entity with the specified position and velocity.
   *
   *  @param position the initial position of the spaceship
   *  @param velocity the initial velocity of the spaceship
   *  @return a tuple containing the unique identifier of the created spaceship entity and its associated list of components
   */
  def createSpaceship(
      position: Point2D,
      velocity: Vector2D,
      entityId: EntityId = EntityId.generate(),
      weapon: Weapon = Weapon.default
  ): (EntityId, List[Component]) =
    val components: List[Component] = List(
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Spaceship),
      ShootingComponent(weapon = weapon)
    )
    (entityId, components)

    /** Creates a new bullet entity with the specified position, velocity, and power.
     *
     *  @param shooterId the unique identifier of the entity that shot the bullet
     *  @param position the initial position of the bullet
     *  @param velocity the initial velocity of the bullet
     *  @param power the power of the bullet
     *  @return a tuple containing the unique identifier of the created bullet entity and its associated list of components
     */
  def createBullet(
      shooterId: EntityId,
      position: Point2D,
      velocity: Vector2D,
      power: Double,
      entityId: EntityId = EntityId.generate()
  ): EntityWithComponents =
    val components: List[Component] = List(
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Bullet),
      BulletComponent(power, shooterId)
    )
    (entityId, components)
