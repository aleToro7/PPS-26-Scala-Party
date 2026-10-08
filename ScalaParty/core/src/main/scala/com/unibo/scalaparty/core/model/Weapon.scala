package com.unibo.scalaparty.core.model

/** The weapon a spaceship fires its bullets with.
 *
 *  @param bulletPower   the damage dealt by each bullet
 *  @param bulletSpeed   the linear movement speed of the bullets
 *  @param shootCooldown the minimum delay between two shots, in milliseconds
 *  @param muzzleOffset  the distance from the shooter's center at which bullets are spawned (the spaceship's nose)
 */
final case class Weapon(
    bulletPower: Double,
    bulletSpeed: Double,
    shootCooldown: Long,
    muzzleOffset: Double
):
  require(bulletPower > 0.0, "Bullet power must be positive")
  require(bulletSpeed > 0.0, "Bullet speed must be positive")
  require(shootCooldown >= 0L, "Shoot cooldown cannot be negative")
  require(muzzleOffset >= 0.0, "Muzzle offset cannot be negative")

object Weapon:
  /** The weapon spaceships spawn with unless configured otherwise. */
  val default: Weapon = Weapon(
    bulletPower = 15.0,
    bulletSpeed = 300.0,
    shootCooldown = 500L,
    muzzleOffset = 20.0
  )
