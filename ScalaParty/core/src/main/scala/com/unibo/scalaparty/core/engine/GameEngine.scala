package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{toDto, EntityDto}
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.systems.*
import com.unibo.scalaparty.core.engine.input.InputGateway
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.{ArenaSettings, GameCommand, MatchOutcome}

/** A trait representing the game engine responsible for updating the state of the game world based on player commands and elapsed time. */
trait GameEngine:
  /** Updates the state of the game world based on the provided player commands and the elapsed time.
   *
   *  @param list  a list of player commands to be processed
   *  @param dt    the elapsed time since the last update, in milliseconds
   *  @return the entities representing the updated state of the game world, along with the events occurred meanwhile
   */
  def update(list: List[GameCommand], dt: Long): TickResult

  /** Tells whether the match is over, according to the [[MatchRules]].
   *
   *  @return how the match ended, or `None` while it is still going on
   */
  def outcome: Option[MatchOutcome]

object GameEngine:
  /** Creates a new instance of the game engine based on the provided game configuration.
   *
   *  @param config the game configuration
   *  @return a new instance of GameEngine
   */
  def apply(config: GameConfig): GameEngine =
    GameEngine(config, SystemPipeline.default(config.settings))

  /** Creates a new instance of the game engine based on the provided game configuration and system pipeline.
   *
   *  @param config   the game configuration
   *  @param pipeline the system pipeline to be used for updating the game world
   *  @return a new instance of GameEngine
   */
  def apply(config: GameConfig, pipeline: SystemPipeline): GameEngine =
    new SinglePlayerGameEngine(config, pipeline)

private class SinglePlayerGameEngine(config: GameConfig, pipeline: SystemPipeline) extends GameEngine:

  private var world: GameWorld = initializeWorld(config)

  /** Simulated time since the match began, in milliseconds. */
  private var elapsed: Long = 0L

  /** The spaceships of the players the match started with. */
  private val players: Set[EntityId] = config.players.toSet

  /** Spawns one spaceship for each player, each on its own spawn point. */
  private def initializeWorld(config: GameConfig): GameWorld =
    val spaceship = config.settings.spaceship
    val spawns = SinglePlayerGameEngine.spawnPoints(config.players.size, config.settings.arena)

    GameWorld(config.players.zip(spawns).map { case (playerId, (position, heading)) =>
      EntityFactory.createSpaceship(
        position = position,
        velocity = Vector2D(spaceship.speed, 0).rotated(heading),
        entityId = playerId,
        weapon = Weapon.fromSettings(config.settings.shooting),
        maxHealth = spaceship.maxHealth,
        collisionDamage = spaceship.collisionDamage,
        rotation = heading
      )
    })

  override def update(list: List[GameCommand], dt: Long): TickResult =
    // Process player commands and update the world state
    world = InputGateway.processCommands(this.world, list)
    // Execute pipeline of systems
    val (updatedWorld, events) = pipeline
      .toList
      .foldLeft((world, Set.empty[GameEvent])):
        case ((currentWorld, events), system) => system.update(currentWorld, events, dt)
    world = updatedWorld
    elapsed += dt
    TickResult(world.serialized, events)

  override def outcome: Option[MatchOutcome] =
    MatchRules.outcome(config.settings.matchSettings, players, survivors, elapsed)

  /** The spaceships of the players still in the world, those destroyed or left being removed from it. */
  private def survivors: Set[EntityId] =
    players.filter(world.findComponents(_).isDefined)

private object SinglePlayerGameEngine:

  /** Where the given number of players enter the arena, as positions and headings in degrees.
   *
   *  A lone player starts in the center of the arena, which is the origin. Several players start evenly spaced on a
   *  circle around the center, each facing it, so that nobody starts next to or aiming at a wall. This placement only
   *  lasts until the game map provides its own spawn points.
   *
   *  @param players how many players enter the arena
   *  @param arena   the bounds of the arena
   *  @return one spawn point for each player
   */
  def spawnPoints(players: Int, arena: ArenaSettings): List[(Point2D, Double)] =
    val center = Point2D.origin
    if players == 1 then List((center, 0.0))
    else
      val radius = Math.min(arena.width, arena.height) / 4
      List.tabulate(players): index =>
        val angle = 180.0 + 360.0 * index / players
        (center + Vector2D(radius, 0).rotated(angle), (angle + 180.0) % 360.0)

extension (world: GameWorld)

  /** Converts the entities in the game world to their corresponding DTO representations.
   *
   *  @return a list of EntityDto representing the entities in the game world
   */
  def serialized: List[EntityDto] = world.entitiesWithComponents
    .map(_.toDto)
    .collect:
      case Some(dto) => dto
