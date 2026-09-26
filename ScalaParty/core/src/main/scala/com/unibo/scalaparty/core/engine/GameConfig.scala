package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.GameSettings

/** Configuration for the game engine.
 *
 *  @param settings               the default game settings for world size and components
 *  @param players                the list of player entity IDs in the match
 */
final case class GameConfig(
    settings: GameSettings,
    players: List[EntityId],
):
  export settings.* // VALUTARE SE MANTENERE

object GameConfig:
  /** Helper to quickly create a single-player configuration.
   *
   *  @param playerId               the unique identifier of the single player
   *  @param settings               the default game settings for world size and components
   *  @return a new [[GameConfig]] configured for a single player
   */
  def singlePlayer(
      playerId: EntityId,
      settings: GameSettings = GameSettings.default,
  ): GameConfig =
    GameConfig(
      players = List(playerId),
      settings = settings,
    )
