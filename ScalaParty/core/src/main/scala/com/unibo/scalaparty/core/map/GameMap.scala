package com.unibo.scalaparty.core.map

import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.model.ArenaSettings

/** A location where an entity enters the arena at the beginning of a match.
 *
 *  @param position the initial position of the entity
 *  @param heading  the initial facing direction, in degrees, measured counter-clockwise from the positive X axis
 */
final case class SpawnPoint(position: Point2D, heading: Double)

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
