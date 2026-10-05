package com.unibo.scalaparty.core.ecs

/** A collectible bonus granting a special advantage to the spaceship that picks it up.
 *
 *  @param name   the unique name identifying the power-up, also shown to the clients
 *  @param effect what the power-up does to the spaceship that picks it up
 */
final case class PowerUp(name: String, effect: Effect):
  require(!name.isBlank, "Power-up name cannot be blank")

/** What a power-up does to the spaceship that picks it up. */
sealed trait Effect

object Effect:

  /** Temporarily modifies one stat of the spaceship.
   *
   *  @param modifier how the stat is modified
   *  @param duration how long the modification lasts, in milliseconds
   */
  final case class Boost(modifier: StatModifier, duration: Long) extends Effect:
    require(duration > 0L, "Boost duration must be positive")

  /** Instantly restores part of the health of the spaceship.
   *
   *  @param amount the health points to restore
   */
  final case class Repair(amount: Double) extends Effect:
    require(amount > 0.0, "Repair amount must be positive")

/** The stats of a spaceship that a power-up can modify. Each system applies the stats it is responsible for. */
enum Stat:
  /** The delay between two shots. */
  case ShootCooldown

  /** The damage dealt by each bullet. */
  case BulletPower

  /** The movement speed. */
  case Speed

  /** The share of the incoming damage actually suffered. */
  case DamageTaken

/** Scales one stat of a spaceship by a constant factor.
 *
 *  @param stat   the stat to scale
 *  @param factor the positive multiplier applied to the stat
 */
final case class StatModifier(stat: Stat, factor: Double):
  require(factor > 0.0, "Modifier factor must be positive")

/** A stat modifier currently affecting a spaceship.
 *
 *  @param modifier  how the stat is modified
 *  @param remaining how long the modification still lasts, in milliseconds
 */
final case class ActiveEffect(modifier: StatModifier, remaining: Long):
  require(remaining > 0L, "An active effect must have some time left")

  /** Lets the given time pass.
   *
   *  @param dt the time to let pass, in milliseconds
   *  @return the effect with less time left, or None if it runs out
   */
  def advanced(dt: Long): Option[ActiveEffect] =
    Option.when(remaining > dt)(copy(remaining = remaining - dt))
