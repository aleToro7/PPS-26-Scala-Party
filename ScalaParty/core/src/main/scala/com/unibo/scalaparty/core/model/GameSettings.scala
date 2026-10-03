package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.model.map.GameMap

/** Configuration settings for spaceship dynamics.
 *
 *  @param speed           the constant movement speed of spaceships
 *  @param rotationSpeed   the angular rotation speed applied when changing direction
 *  @param maxHealth       the health points spaceships spawn with
 *  @param collisionDamage the damage a spaceship deals to the entities it collides with
 */
final case class SpaceshipSettings(
    speed: Double = 120.0,
    rotationSpeed: Double = 180.0,
    maxHealth: Double = 100.0,
    collisionDamage: Double = 20.0
):
  require(speed > 0.0, "Spaceship speed must be positive")
  require(rotationSpeed > 0.0, "Rotation speed must be positive")
  require(maxHealth > 0.0, "Max health must be positive")
  require(collisionDamage >= 0.0, "Collision damage cannot be negative")

/** Configuration settings for weapons and projectile dynamics.
 *
 *  @param bulletPower   the damage or impact power of the bullets
 *  @param bulletSpeed   the linear movement speed of the bullets
 *  @param shootCooldown the minimum cooldown delay between shots, in milliseconds
 *  @param muzzleOffset  the distance from the shooter's center at which bullets are spawned (the spaceship's nose)
 */
final case class ShootingSettings(
    bulletPower: Double = 15.0,
    bulletSpeed: Double = 300.0,
    shootCooldown: Long = 500L,
    muzzleOffset: Double = 20.0
):
  require(bulletPower > 0.0, "Bullet power must be positive")
  require(bulletSpeed > 0.0, "Bullet speed must be positive")
  require(shootCooldown >= 0L, "Shoot cooldown cannot be negative")
  require(muzzleOffset >= 0.0, "Muzzle offset cannot be negative")

/** Unified configuration grouping all arena, entity, and gameplay mechanics parameters.
 *
 *  @param spaceship settings controlling spaceship dynamics
 *  @param shooting  settings controlling weapon firing and bullet behavior
 *  @param map      the map layout and spawn points for the game world
 */
final case class GameSettings(
    spaceship: SpaceshipSettings = SpaceshipSettings(),
    shooting: ShootingSettings = ShootingSettings(),
    map: GameMap = GameMap.default
)

object GameSettings:
  val default: GameSettings = GameSettings()
