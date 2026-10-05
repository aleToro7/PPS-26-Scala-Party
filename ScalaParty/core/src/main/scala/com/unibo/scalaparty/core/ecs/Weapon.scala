package com.unibo.scalaparty.core.ecs
import com.unibo.scalaparty.core.model.ShootingSettings

/** Weapon specifications for entity firing capabilities.
 *
 *  @param bulletPower   the damage/impact power of the bullet
 *  @param bulletSpeed   the linear velocity of the bullet
 *  @param shootCooldown the delay in milliseconds before the next shot
 *  @param muzzleOffset  the distance from the shooter's center at which bullets are spawned
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

  /** Applies the given power-up effects to this weapon.
   *
   *  @param effects the effects in action on the entity holding the weapon
   *  @return a new weapon whose cooldown and bullet power are scaled by the effects
   */
  def boostedBy(effects: ActiveEffectsComponent): Weapon =
    copy(
      shootCooldown = math.round(shootCooldown * effects.factorOf(Stat.ShootCooldown)),
      bulletPower = bulletPower * effects.factorOf(Stat.BulletPower)
    )

object Weapon:

  /** Standard baseline weapon instance. */
  val default: Weapon = fromSettings(ShootingSettings())

  /** Creates a weapon specification directly from domain shooting settings. */
  def fromSettings(settings: ShootingSettings): Weapon =
    Weapon(
      bulletPower = settings.bulletPower,
      bulletSpeed = settings.bulletSpeed,
      shootCooldown = settings.shootCooldown,
      muzzleOffset = settings.muzzleOffset
    )
