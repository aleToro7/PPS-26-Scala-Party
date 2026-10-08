package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.model.map.GameMap

/** Configuration settings for spaceship dynamics.
 *
 *  @param speed           the constant movement speed of spaceships
 *  @param rotationSpeed   the angular rotation speed applied when changing direction
 *  @param maxHealth       the health points spaceships spawn with
 *  @param collisionDamage the damage a spaceship deals to the entities it collides with
 *  @param weapon          the weapon spaceships spawn with
 */
final case class SpaceshipSettings(
    speed: Double,
    rotationSpeed: Double,
    maxHealth: Double,
    collisionDamage: Double,
    weapon: Weapon
):
  require(speed > 0.0, "Spaceship speed must be positive")
  require(rotationSpeed > 0.0, "Rotation speed must be positive")
  require(maxHealth > 0.0, "Max health must be positive")
  require(collisionDamage >= 0.0, "Collision damage cannot be negative")

object SpaceshipSettings:
  /** The spaceship dynamics used unless configured otherwise. */
  val default: SpaceshipSettings = SpaceshipSettings(
    speed = 120.0,
    rotationSpeed = 180.0,
    maxHealth = 100.0,
    collisionDamage = 1.0,
    weapon = Weapon.default
  )

/** Configuration settings for the rules deciding when a match is over.
 *
 *  @param timeLimit the longest a match can last, in milliseconds of simulated time
 */
final case class MatchSettings(
    timeLimit: Long
):
  require(timeLimit > 0L, "Time limit must be positive")

object MatchSettings:
  /** The match rules used unless configured otherwise. */
  val default: MatchSettings = MatchSettings(timeLimit = 180_000L)

/** Unified configuration grouping all arena, entity, and gameplay mechanics parameters.
 *
 *  @param spaceship     settings controlling spaceship dynamics, including the weapon they fire with
 *  @param map           the map layout and spawn points for the game world
 *  @param matchSettings settings controlling when a match ends
 */
final case class GameSettings(
    spaceship: SpaceshipSettings,
    map: GameMap,
    matchSettings: MatchSettings
)

object GameSettings:
  /** The configuration used unless configured otherwise. */
  val default: GameSettings = GameSettings(
    spaceship = SpaceshipSettings.default,
    map = GameMap.default,
    matchSettings = MatchSettings.default
  )
