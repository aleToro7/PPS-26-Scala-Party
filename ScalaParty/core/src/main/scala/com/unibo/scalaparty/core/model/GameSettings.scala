package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.geometry.Point2D

/** Configuration settings for the game arena.
 *
 *  @param width  the horizontal width of the arena
 *  @param height the vertical height of the arena
 */
final case class ArenaSettings(
    width: Int = 800,
    height: Int = 800
):
  require(width > 0 && height > 0, "Arena dimensions must be positive")

  /** Checks whether a point lies within the arena bounds, edges included.
   *
   *  @param point the point to check
   *  @return true if the point is inside the arena, false otherwise
   */
  def contains(point: Point2D): Boolean =
    point.x >= 0 && point.x <= width && point.y >= 0 && point.y <= height

/** Configuration settings for spaceship dynamics.
 *
 *  @param speed         the constant movement speed of spaceships
 *  @param rotationSpeed the angular rotation speed applied when changing direction
 */
final case class SpaceshipSettings(
    speed: Double = 50.0,
    rotationSpeed: Double = 180.0
):
  require(speed > 0.0, "Spaceship speed must be positive")
  require(rotationSpeed > 0.0, "Rotation speed must be positive")

/** Configuration settings for weapons and projectile dynamics.
 *
 *  @param bulletPower   the damage or impact power of the bullets
 *  @param bulletSpeed   the linear movement speed of the bullets
 *  @param shootCooldown the minimum cooldown delay between shots, in milliseconds
 *  @param muzzleOffset  the distance from the shooter's center at which bullets are spawned (the spaceship's nose)
 */
final case class ShootingSettings(
    bulletPower: Double = 10.0,
    bulletSpeed: Double = 100.0,
    shootCooldown: Long = 250L,
    muzzleOffset: Double = 12.0
):
  require(bulletPower > 0.0, "Bullet power must be positive")
  require(bulletSpeed > 0.0, "Bullet speed must be positive")
  require(shootCooldown >= 0L, "Shoot cooldown cannot be negative")
  require(muzzleOffset >= 0.0, "Muzzle offset cannot be negative")

/** Unified configuration grouping all arena, entity, and gameplay mechanics parameters.
 *
 *  @param arena     settings controlling arena bounds
 *  @param spaceship settings controlling spaceship dynamics
 *  @param shooting  settings controlling weapon firing and bullet behavior
 */
final case class GameSettings(
    arena: ArenaSettings = ArenaSettings(),
    spaceship: SpaceshipSettings = SpaceshipSettings(),
    shooting: ShootingSettings = ShootingSettings()
)

object GameSettings:
  val default: GameSettings = GameSettings()
