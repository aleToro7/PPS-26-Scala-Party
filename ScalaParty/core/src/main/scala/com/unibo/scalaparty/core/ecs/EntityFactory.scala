package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D, given}
import com.unibo.scalaparty.core.model.{GameSettings, SpaceshipSettings, Weapon}

object EntityFactory:

  /** Creates a new spaceship entity with the specified position and velocity.
   *
   *  @param position the initial position of the spaceship
   *  @param velocity the initial velocity of the spaceship
   *  @param maxHealth the health points the spaceship spawns with
   *  @param collisionDamage the damage the spaceship deals to the entities it collides with
   *  @return a tuple containing the unique identifier of the created spaceship entity and its associated list of components
   */
  def createSpaceship(
      position: Point2D,
      velocity: Vector2D,
      entityId: EntityId = EntityId.generate(),
      weapon: Weapon = Weapon(),
      maxHealth: Double = GameSettings.default.spaceship.maxHealth,
      collisionDamage: Double = GameSettings.default.spaceship.collisionDamage
  ): (EntityId, List[Component]) =
    val components: List[Component] = List(
      PositionComponent(position),
      MovementComponent(velocity),
      RotationComponent(velocity.angle),
      EntityTypeComponent(EntityType.Spaceship),
      ShootingComponent(weapon = weapon),
      EntityTypeComponent(EntityType.Spaceship),
      ShapeComponent(Shape.Polygon((12.0, 0.0), (-12.0, -7.0), (-12.0, 7.0))),
      HealthComponent.full(maxHealth),
      CollisionDamageComponent(collisionDamage)
    )
    (entityId, components)

  def createSpaceshipFromConfig(settings: SpaceshipSettings)(
      position: Point2D,
      direction: Vector2D,
      entityId: EntityId = EntityId.generate()
  ): EntityWithComponents =
    createSpaceship(
      position = position,
      velocity = direction.normalized * settings.speed,
      entityId = entityId,
      weapon = settings.weapon,
      maxHealth = settings.maxHealth,
      collisionDamage = settings.collisionDamage
    )

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
      ShapeComponent(Shape.Circle(3.0, (0.0, 0.0)))
    )
    (entityId, components)

  /** Creates a new wall entity with the specified position, width, and height.
   *
   *  @param position the position of the wall
   *  @param width the width of the wall
   *  @param height the height of the wall
   *  @return a tuple containing the unique identifier of the created wall entity and its associated list of components
   */
  def createWall(
      position: Point2D,
      width: Double,
      height: Double,
      entityId: EntityId = EntityId.generate()
  ): EntityWithComponents =
    val components: List[Component] = List(
      PositionComponent(position),
      EntityTypeComponent(EntityType.Wall),
      ShapeComponent(Shape.AABB(width, height, (0.0, 0.0)))
    )
    (entityId, components)

  /** Creates a new match clock entity, set at the beginning of the match.
   *
   *  @param entityId the unique identifier of the clock
   *  @return a tuple containing the unique identifier of the created clock entity and its associated list of components
   */
  def createMatchClock(entityId: EntityId = EntityId.generate()): EntityWithComponents =
    (entityId, List(MatchClockComponent()))
