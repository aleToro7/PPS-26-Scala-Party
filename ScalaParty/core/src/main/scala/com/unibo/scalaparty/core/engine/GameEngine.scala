package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{toDto, EntityDto, PlayerCommand}
import com.unibo.scalaparty.core.ecs.{EntityFactory, EntityId, GameWorld}
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

/** A trait representing the game engine responsible for updating the state of the game world based on player commands and elapsed time. */
trait GameEngine:
  /** Updates the state of the game world based on the provided player commands and the elapsed time.
   *
   * @param list  a list of player commands to be processed
   * @param dt    the elapsed time since the last update, in milliseconds
   * @return a new list of entities representing the updated state of the game world
   */
  def update(list: List[PlayerCommand], dt: Long): List[EntityDto]

object GameEngine:
  /** Creates a new instance of the game engine.
   *
   *  @param player the unique identifier of the player's entity
   *  @return a new instance of GameEngine
   */
  def apply(player: EntityId): GameEngine = new SinglePlayerGameEngine(SinglePlayerConfig(player, 800, 600, 5.0, 0.5))

private class SinglePlayerGameEngine(config: SinglePlayerConfig) extends GameEngine:

  private val world: GameWorld = initializeWorld(config)

  private def initializeWorld(config: SinglePlayerConfig): GameWorld =
    val playerSpaceship = EntityFactory.createSpaceship(
      entityId = config.player,
      position = Point2D(config.worldWidth / 2, config.worldHeight / 2),
      velocity = Vector2D(config.spaceshipSpeed, 0)
    )
    GameWorld(List(playerSpaceship))

  override def update(list: List[PlayerCommand], dt: Long): List[EntityDto] =
    world.entitiesWithComponents
      .map(_.toDto)
      .collect { case Some(dto) => dto }
