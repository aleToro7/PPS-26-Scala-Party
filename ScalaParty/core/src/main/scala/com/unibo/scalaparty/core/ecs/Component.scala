package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

/** A marker trait for all components in the Entity-Component-System (ECS) architecture.
 *  A Component represents a specific aspect of an entity's state or behavior, such as position, movement, health, etc.
 */
trait Component

/** Represents the movement aspect of an entity, encapsulating its velocity in a two-dimensional space.
 *  @param velocity the velocity of the entity
 */
case class MovementComponent(velocity: Vector2D) extends Component

/** Represents the position aspect of an entity in a two-dimensional space.
 *  @param position the position of the entity
 */
case class PositionComponent(position: Point2D) extends Component

/** Represents an entity's type, which can be used to categorize entities in the game world.
 *  @param entityType the type of the entity
 */
case class EntityTypeComponent(entityType: EntityType) extends Component

/** Represents an entity's capacity to shoot.
 *  @param bulletPower the power of the bullets
 *  @param bulletSpeed the speed of the bullets
 *  @param shootCooldown the cooldown time between shots
 *  @param isShooting whether the entity is currently shooting
 *  @param lastShootTime the last time the entity shot
 */
case class ShootingComponent(
    bulletPower: Double,
    bulletSpeed: Double,
    shootCooldown: Long,
    isShooting: Boolean = false,
    lastShootTime: Long = 0L
) extends Component

/** Represents a bullet's info, including its power and owner.
 *  @param power the power of the bullet
 *  @param shooterId the unique identifier of the entity that shot the bullet
 */
case class BulletComponent(power: Double, shooterId: EntityId) extends Component
