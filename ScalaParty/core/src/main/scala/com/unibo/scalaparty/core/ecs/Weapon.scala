package com.unibo.scalaparty.core.ecs
import com.unibo.scalaparty.core.model.ShootingSettings

/** Weapon specifications for entity firing capabilities.
 *
 *  @param bulletPower   the damage/impact power of the bullet
 *  @param bulletSpeed   the linear velocity of the bullet
 *  @param shootCooldown the delay in milliseconds before the next shot
 */
final case class Weapon(  //enum o trait extended by multiple case class in the eventuality of powerup implementation
                             bulletPower: Double,
                             bulletSpeed: Double,
                             shootCooldown: Long
                           ):
  require(bulletPower > 0.0, "Bullet power must be positive")
  require(bulletSpeed > 0.0, "Bullet speed must be positive")
  require(shootCooldown >= 0L, "Shoot cooldown cannot be negative")

object Weapon:

  /** Standard baseline weapon instance. */
  val default: Weapon = fromSettings(ShootingSettings())

  /** Creates a weapon specification directly from domain shooting settings. */
  def fromSettings(settings: ShootingSettings): Weapon =
    Weapon(
      bulletPower = settings.bulletPower,
      bulletSpeed = settings.bulletSpeed,
      shootCooldown = settings.shootCooldown
    )
