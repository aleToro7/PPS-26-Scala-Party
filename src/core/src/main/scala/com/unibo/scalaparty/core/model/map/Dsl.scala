package com.unibo.scalaparty.core.model.map

import scala.compiletime.ops.int.+

/** A MapTile represents the type of tile in the game map.
 *  It can be Empty, Wall, or Spawn.
 */
enum MapTile:
  /** An empty tile. */
  case Empty

  /** A wall tile. */
  case Wall

  /** A spawn tile. */
  case Spawn

object Dsl:
  /** Alias for [[MapTile.Spawn]] */
  val S: MapTile = MapTile.Spawn

  /** Alias for [[MapTile.Wall]] */
  val W: MapTile = MapTile.Wall

  /** Alias for [[MapTile.Empty]] */
  val / : MapTile = MapTile.Empty

  /** A MapRow represents a row of MapTiles in the game map.
   *  @tparam N the number of tiles in the row
   */
  opaque type MapRow[N <: Int] <: Vector[MapTile] = Vector[MapTile]

  extension (firstTile: MapTile)

    /** Combine two MapTiles into a MapRow of length 2.
     *  @param secondTile the second MapTile to combine
     *  @return a MapRow containing the two tiles
     */
    def |(secondTile: MapTile): MapRow[2] = Vector(firstTile, secondTile)

  extension [N <: Int](row: MapRow[N])

    /** Add a MapTile to the end of the MapRow, increasing its length by 1.
     *  @param tile the MapTile to add
     *  @return a new MapRow with the added tile
     */
    def |(tile: MapTile): MapRow[N + 1] = tiles :+ tile

    private def tiles: Vector[MapTile] = row
