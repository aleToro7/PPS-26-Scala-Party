package com.unibo.scalaparty.core.map

import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.model.ArenaSettings

/** A location where an entity enters the arena at the beginning of a match.
 *
 *  @param position the initial position of the entity
 *  @param heading  the initial facing direction, in degrees, measured counter-clockwise from the positive X axis
 */
final case class SpawnPoint(position: Point2D, heading: Double)

object SpawnPoint:

  /** Creates a spawn point facing towards a given target.
   *
   *  @param position the initial position of the entity
   *  @param target   the point the entity initially faces
   *  @return a new [[SpawnPoint]] whose heading points from `position` to `target`
   */
  def facing(position: Point2D, target: Point2D): SpawnPoint =
    val direction = target - position
    SpawnPoint(position, Math.toDegrees(Math.atan2(direction.y, direction.x)))

/** The static layout of an arena in which a match takes place.
 *
 *  @param arena        the bounds of the arena
 *  @param playerSpawns the spawn points available to players, one per player
 */
final case class GameMap(arena: ArenaSettings, playerSpawns: List[SpawnPoint]):
  require(playerSpawns.nonEmpty, "A game map must define at least one player spawn point")
  require(playerSpawns.forall(spawn => arena.contains(spawn.position)), "Player spawn points must lie within the arena")

  /** The maximum number of players the map can host. */
  def capacity: Int = playerSpawns.size

object GameMap:

  /** An empty arena with four player spawn points placed symmetrically around the center, all facing it.
   *  Spawn points are ordered so that any prefix of them keeps players on opposite sides of the arena.
   */
  val default: GameMap =
    val arena = ArenaSettings()
    val (near, far) = (0.25, 0.75)
    val center = Point2D(arena.width * 0.5, arena.height * 0.5)
    val spawns = List((near, near), (far, far), (far, near), (near, far)).map: (x, y) =>
      SpawnPoint.facing(Point2D(arena.width * x, arena.height * y), center)
    GameMap(arena, spawns)
