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
 *  @param weapon specify the shot's values to apply
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
case class RotationComponent(angle: Double = 0.0) extends Component

/** Represents the health of an entity that can be damaged.
 *  @param current the remaining health points, between zero and max
 *  @param max the maximum health points
 */
case class HealthComponent(current: Double, max: Double) extends Component:
  require(max > 0.0, "Max health must be positive")
  require(current >= 0.0 && current <= max, "Current health must be between zero and max health")

  /** Applies the given damage, never letting the health drop below zero.
   *  @param amount the non-negative damage to apply
   *  @return a new component with the reduced health
   */
  def damaged(amount: Double): HealthComponent =
    require(amount >= 0.0, "Damage cannot be negative")
    copy(current = math.max(0.0, current - amount))

  /** Whether the health has been completely depleted.
   *  @return true if no health points are left, false otherwise
   */
  def isDepleted: Boolean = current == 0.0

object HealthComponent:
  /** Creates a health component at full health.
   *  @param max the maximum health points
   *  @return a new component whose current health equals its maximum
   */
  def full(max: Double): HealthComponent = HealthComponent(max, max)

/** Represents the damage an entity deals to the entities it collides with.
 *  @param damage the damage dealt on impact
 */
case class CollisionDamageComponent(damage: Double) extends Component:
  require(damage >= 0.0, "Collision damage cannot be negative")

/** Represents how long a match has been going on.
 *  @param elapsed the simulated time since the match began, in milliseconds
 */
case class MatchClockComponent(elapsed: Long = 0L) extends Component:
  require(elapsed >= 0L, "Elapsed time cannot be negative")

  /** Lets the given time pass.
   *  @param dt the non-negative time to let pass, in milliseconds
   *  @return a new component whose elapsed time is increased by dt
   */
  def advanced(dt: Long): MatchClockComponent =
    require(dt >= 0L, "Time cannot flow backwards")
    copy(elapsed = elapsed + dt)

/** Represents the power-up effects currently affecting an entity.
 *  @param effects the stat modifiers still in action
 */
case class ActiveEffectsComponent(effects: List[ActiveEffect] = Nil) extends Component:

  /** Starts the given boost, on top of the effects already in action.
   *  @param boost the boost to start
   *  @return a new component including the boost for its whole duration
   */
  def activated(boost: Effect.Boost): ActiveEffectsComponent =
    copy(effects = ActiveEffect(boost.modifier, boost.duration) :: effects)

  /** Lets the given time pass, dropping the effects that run out.
   *  @param dt the non-negative time to let pass, in milliseconds
   *  @return a new component with only the effects still in action
   */
  def advanced(dt: Long): ActiveEffectsComponent =
    require(dt >= 0L, "Time cannot flow backwards")
    copy(effects = effects.flatMap(_.advanced(dt)))

  /** Combines the factors of all the effects in action on the given stat.
   *  @param stat the stat to inspect
   *  @return the product of the factors applied to the stat, or 1.0 if no effect modifies it
   */
  def factorOf(stat: Stat): Double =
    effects.map(_.modifier).collect { case StatModifier(`stat`, factor) => factor }.product
