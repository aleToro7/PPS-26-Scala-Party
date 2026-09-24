package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.{MovementSystem, ShootingSystem, SystemPipeline}
import com.unibo.scalaparty.core.model.GameSettings

/** Configuration for the game engine.
 *
 *  @param settings               the default game settings for world size and components
 *  @param players                the list of player entity IDs in the match
 *  @param pipeline               the ordered pipeline of systems to execute sequentially
 */
final case class GameConfig(
    settings: GameSettings,
    players: List[EntityId],
    pipeline: SystemPipeline = GameConfig.defaultPipeline
):
  export settings.* // VALUTARE SE MANTENERE

object GameConfig:
  private val defaultPipeline = MovementSystem >> ShootingSystem

  /** Helper to quickly create a single-player configuration.
   *
   *  @param playerId               the unique identifier of the single player
   *  @param settings               the default game settings for world size and components
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
