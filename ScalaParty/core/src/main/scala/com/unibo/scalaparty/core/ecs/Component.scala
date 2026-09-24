package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D}

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
 *  @param cooldownTimer the current cooldown timer
 */
case class ShootingComponent(
    weapon: Weapon = Weapon.default,
    isShooting: Boolean = false,
    cooldownTimer: Long = 0L
) extends Component

/** Represents a bullet's info, including its power and owner.
 *  @param power the power of the bullet
 *  @param shooterId the unique identifier of the entity that shot the bullet
 */
case class BulletComponent(power: Double, shooterId: EntityId) extends Component

/** Represents a shape associated with an entity.
 *  @param shape the shape of the entity
 */
case class ShapeComponent(shape: Shape) extends Component

/** Represents the rotation of an entity.
 *  @param angle the angle of rotation in degrees
 */
case class RotationComponent(angle: Double) extends Component
