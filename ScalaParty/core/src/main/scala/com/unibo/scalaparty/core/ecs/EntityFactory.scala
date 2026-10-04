package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D, given}
import com.unibo.scalaparty.core.model.GameSettings

object EntityFactory:

  /** Creates a new spaceship entity with the specified position and velocity.
   *
   *  @param position the initial position of the spaceship
   *  @param velocity the initial velocity of the spaceship
   *  @param maxHealth the health points the spaceship spawns with
   *  @param collisionDamage the damage the spaceship deals to the entities it collides with
   *  @param rotation the angle in degrees the spaceship initially faces, which should match its velocity
   *  @return a tuple containing the unique identifier of the created spaceship entity and its associated list of components
   */
  def createSpaceship(
      position: Point2D,
      velocity: Vector2D,
      entityId: EntityId = EntityId.generate(),
      weapon: Weapon = Weapon.default,
      maxHealth: Double = GameSettings.default.spaceship.maxHealth,
      collisionDamage: Double = GameSettings.default.spaceship.collisionDamage,
      rotation: Double = 0.0
  ): (EntityId, List[Component]) =
    val components: List[Component] = List(
      PositionComponent(position),
      MovementComponent(velocity),
      RotationComponent(rotation),
      EntityTypeComponent(EntityType.Spaceship),
      ShootingComponent(weapon = weapon),
      EntityTypeComponent(EntityType.Spaceship),
      ShapeComponent(Shape.Polygon((12.0, 0.0), (-12.0, -7.0), (-12.0, 7.0))),
      HealthComponent.full(maxHealth),
      CollisionDamageComponent(collisionDamage)
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
      BulletComponent(power, shooterId),
      ShapeComponent(Shape.Circle(1.0, (0.0, 0.0)))
    )
    (entityId, components)

  /** Creates a new match clock entity, set at the beginning of the match.
   *
   *  @param entityId the unique identifier of the clock
   *  @return a tuple containing the unique identifier of the created clock entity and its associated list of components
   */
  def createMatchClock(entityId: EntityId = EntityId.generate()): EntityWithComponents =
    (entityId, List(MatchClockComponent()))
