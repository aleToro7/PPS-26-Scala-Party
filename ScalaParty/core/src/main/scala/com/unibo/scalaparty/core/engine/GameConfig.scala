package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.{DamageSystem, MovementSystem, ShootingSystem, SystemPipeline}
import com.unibo.scalaparty.core.map.GameMap
import com.unibo.scalaparty.core.model.GameSettings

/** Configuration for the game engine.
 *
 *  @param settings               the game settings for entities and gameplay mechanics
 *  @param players                the list of player entity IDs in the match
 *  @param map                    the map the match is played on, providing one spawn point per player
 *  @param pipeline               the ordered pipeline of systems to execute sequentially
 */
final case class GameConfig(
    settings: GameSettings,
    players: List[EntityId],
    map: GameMap = GameMap.default,
    pipeline: SystemPipeline = GameConfig.defaultPipeline
):
  require(players.size <= map.capacity, s"The map hosts at most ${map.capacity} players, got ${players.size}")

object GameConfig:
  private val defaultPipeline = MovementSystem >> ShootingSystem >> DamageSystem

  /** Helper to quickly create a single-player configuration.
   *
   *  @param playerId               the unique identifier of the single player
   *  @param settings               the game settings for entities and gameplay mechanics
   *  @param pipeline               the ordered pipeline of systems to execute sequentially
   *  @return a new [[GameConfig]] configured for a single player
   */
  def singlePlayer(
      playerId: EntityId,
      settings: GameSettings = GameSettings.default,
      pipeline: SystemPipeline = defaultPipeline
  ): GameConfig =
    GameConfig(
      players = List(playerId),
      settings = settings,
      pipeline = pipeline
    )
