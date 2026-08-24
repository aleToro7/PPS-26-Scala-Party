package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.{MovementSystem, SystemPipeline}

/** Configuration for the game engine.
 *
 *  @param players                the list of player entity IDs in the match
 *  @param worldWidth             the width of the arena
 *  @param worldHeight            the height of the arena
 *  @param spaceshipSpeed         the constant movement speed of spaceships
 *  @param spaceshipRotationSpeed the rotation speed applied when changing direction
 *  @param pipeline               the ordered pipeline of systems to execute sequentially
 */
final case class GameConfig(
    players: List[EntityId],
    worldWidth: Int,
    worldHeight: Int,
    spaceshipSpeed: Double,
    spaceshipRotationSpeed: Double,
    pipeline: SystemPipeline = SystemPipeline(
      MovementSystem
    )
)

object GameConfig:
  private val defaultPipeline = SystemPipeline(
    MovementSystem,
  )

  /** Helper to quickly create a single-player configuration.
   *
   *  @param playerId               the unique identifier of the single player
   *  @param worldWidth             the width of the arena
   *  @param worldHeight            the height of the arena
   *  @param spaceshipSpeed         the constant movement speed of the spaceship
   *  @param spaceshipRotationSpeed the rotation speed applied when changing direction
   *  @param pipeline               the ordered pipeline of systems to execute sequentially
   *  @return a new [[GameConfig]] configured for a single player
   */
  def singlePlayer(
      playerId: EntityId,
      worldWidth: Int = 800,
      worldHeight: Int = 800,
      spaceshipSpeed: Double = 1.0,
      spaceshipRotationSpeed: Double = 180.0,
      pipeline: SystemPipeline = defaultPipeline
  ): GameConfig =
    GameConfig(
      players = List(playerId),
      worldWidth = worldWidth,
      worldHeight = worldHeight,
      spaceshipSpeed = spaceshipSpeed,
      spaceshipRotationSpeed = spaceshipRotationSpeed,
      pipeline = pipeline
    )
