package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{toDto, EntityDto}
import com.unibo.scalaparty.core.ecs.{EntityFactory, EntityId, GameEvent, GameWorld}
import com.unibo.scalaparty.core.engine.input.InputGateway
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.GameCommand

/** A trait representing the game engine responsible for updating the state of the game world based on player commands and elapsed time. */
trait GameEngine:
  /** Updates the state of the game world based on the provided player commands and the elapsed time.
   *
   *  @param list  a list of player commands to be processed
   *  @param dt    the elapsed time since the last update, in milliseconds
   *  @return a new list of entities representing the updated state of the game world
   */
  def update(list: List[GameCommand], dt: Long): List[EntityDto]

object GameEngine:

  /** Creates a new instance of the game engine based on the provided game configuration.
   *
   *  @param config the game configuration
   *  @return a new instance of GameEngine
   */
  def apply(config: GameConfig): GameEngine =
    new SinglePlayerGameEngine(config)

private class SinglePlayerGameEngine(config: GameConfig) extends GameEngine:

  private val world: GameWorld = initializeWorld(config)

  private def initializeWorld(config: GameConfig): GameWorld =
    val playerSpaceship = EntityFactory.createSpaceship(
      entityId = config.players.head,
      position = Point2D(config.worldWidth / 2, config.worldHeight / 2),
      velocity = Vector2D(config.spaceshipSpeed, 0)
    )
    GameWorld(List(playerSpaceship))

  override def update(list: List[GameCommand], dt: Long): List[EntityDto] =
    // Process player commands and update the world state
    val world = InputGateway.processCommands(this.world, list)
    // Execute pipeline of systems
    val (updatedWorld, _) = config.pipeline
      .toList
      .foldLeft((world, Set.empty[GameEvent])):
        case ((currentWorld, events), system) => system.update(currentWorld, events, dt)
    // Return the updated entities as DTOs
    updatedWorld
      .entitiesWithComponents
      .map(_.toDto)
      .collect({ case Some(dto) => dto })
