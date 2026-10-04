package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{toDto, EntityDto}
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.systems.*
import com.unibo.scalaparty.core.engine.input.InputGateway
import com.unibo.scalaparty.core.geometry.Shape
import com.unibo.scalaparty.core.model.GameCommand

/** A trait representing the game engine responsible for updating the state of the game world based on player commands and elapsed time. */
trait GameEngine:
  /** Returns the bounding box of the game arena. */
  def arena: Shape.AABB

  /** Updates the state of the game world based on the provided player commands and the elapsed time.
   *
   *  @param list  a list of player commands to be processed
   *  @param dt    the elapsed time since the last update, in milliseconds
   *  @return the entities representing the updated state of the game world, along with the events occurred meanwhile
   */
  def update(list: List[GameCommand], dt: Long): TickResult

object GameEngine:
  /** Creates a new instance of the game engine based on the provided game configuration.
   *
   *  @param config the game configuration
   *  @return a new instance of GameEngine
   */
  def apply(config: GameConfig): GameEngine =
    GameEngine(config, SystemPipeline.default(config.settings, config.players.size))

  /** Creates a new instance of the game engine based on the provided game configuration and system pipeline.
   *
   *  @param config   the game configuration
   *  @param pipeline the system pipeline to be used for updating the game world
   *  @return a new instance of GameEngine
   */
  def apply(config: GameConfig, pipeline: SystemPipeline): GameEngine =
    new DefaultGameEngine(config, pipeline)

private class DefaultGameEngine(
    config: GameConfig,
    pipeline: SystemPipeline
) extends GameEngine:
  private var world: GameWorld =
    config.map.buildWorld(config.settings)(config.players) + EntityFactory.createMatchClock()
  val arena: Shape.AABB = config.map.shape

  override def update(list: List[GameCommand], dt: Long): TickResult =
    world = InputGateway.processCommands(this.world, list)
    val (updatedWorld, events) = pipeline
      .toList
      .foldLeft((world, Set.empty[GameEvent])):
        case ((currentWorld, events), system) => system.update(currentWorld, events, dt)
    world = updatedWorld
    TickResult(world.serialized, events)

extension (world: GameWorld)

  /** Converts the entities in the game world to their corresponding DTO representations.
   *
   *  @return a list of EntityDto representing the entities in the game world
   */
  def serialized: List[EntityDto] = world.entitiesWithComponents
    .map(_.toDto)
    .collect:
      case Some(dto) => dto
