package com.unibo.scalaparty.core.model

import scala.concurrent.duration.DurationInt

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
    collisionDamage: Double = 1.0
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

/** Configuration settings for the rules deciding when a match is over.
 *
 *  @param timeLimit the longest a match can last, in milliseconds of simulated time
 */
final case class MatchSettings(
    timeLimit: Long = 60_000L
):
  require(timeLimit > 0L, "Time limit must be positive")

/** Configuration settings for the power-ups appearing on the map.
 *
 *  @param catalog      the power-ups that can appear on a spot, each with the same probability
 *  @param respawnDelay the time before a new power-up appears on a spot once picked up, in milliseconds
 *  @param pickupRadius the distance from a spot within which a spaceship picks up its power-up
 *  @param seed         the seed drawing the power-ups of the match: the same seed always yields the same power-ups,
 *                      so it should be chosen at random outside the core to make every match different
 */
final case class PowerUpSettings(
    catalog: List[PowerUp] = PowerUpSettings.defaultCatalog,
    respawnDelay: Long = 10_000L,
    pickupRadius: Double = 25.0,
    seed: Long = 0L
):
  require(catalog.nonEmpty, "Power-up catalog cannot be empty")
  require(respawnDelay > 0L, "Respawn delay must be positive")
  require(pickupRadius > 0.0, "Pickup radius must be positive")

object PowerUpSettings:
  import PowerUpDsl.*

  private val boostDuration = 8.seconds

  /** The power-ups available by default. */
  val defaultCatalog: List[PowerUp] = List(
    powerUp("rapid-fire") lasting boostDuration scaling ShootCooldown by 0.5,
    powerUp("damage") lasting boostDuration scaling BulletPower by 2.0,
    powerUp("shield") lasting boostDuration scaling DamageTaken by 0.25,
    powerUp("repair") healing 30
  )

/** Unified configuration grouping all arena, entity, and gameplay mechanics parameters.
 *
 *  @param spaceship     settings controlling spaceship dynamics
 *  @param shooting      settings controlling weapon firing and bullet behavior
 *  @param map           the map layout and spawn points for the game world
 *  @param matchSettings settings controlling when a match ends
 *  @param powerUps      settings controlling the power-ups appearing on the map
 */
final case class GameSettings(
    spaceship: SpaceshipSettings = SpaceshipSettings(),
    shooting: ShootingSettings = ShootingSettings(),
    map: GameMap = GameMap.default,
    matchSettings: MatchSettings = MatchSettings(),
    powerUps: PowerUpSettings = PowerUpSettings()
)

object GameSettings:
  val default: GameSettings = GameSettings()
