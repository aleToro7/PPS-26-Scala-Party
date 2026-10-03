package com.unibo.scalaparty.core.model.map

import scala.compiletime.ops.int.+

import com.unibo.scalaparty.core.ecs.{EntityFactory, EntityId, GameWorld}
import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D}
import com.unibo.scalaparty.core.model.GameSettings

/** A GameMap represents a layout strategy used to build a GameWorld.
 *  It should define the shape of the map, the placement of walls, and the spawn points for players.
 */
trait GameMap:
  /** Build a GameWorld based on the map's layout.
   *  @param settings The game settings to use for building the world.
   *  @param players The list of players to include in the game world.
   *  @return The constructed game world.
   */
  def buildWorld(settings: GameSettings)(players: Iterable[EntityId]): GameWorld

  /** Get the dimension of the map.
   *  @return The dimension of the map.
   */
  val dimension: Dimension

  /** Get the bounding box shape of the map.
   *  @return The AABB representing the map's boundary.
   */
  def shape: Shape.AABB = Shape.AABB(
    dimension.width.toDouble,
    dimension.height.toDouble,
    Point2D(0.0, 0.0) // Il centro dell'arena è ora (0,0)
  )

object GameMap:

  import Dsl.*

  /** Create a GameMap from a grid of MapTiles.
   *
   *  @param grid The grid of MapTiles representing the map layout.
   *  @return A new GameMap instance.
   */
  def fromGrid[N <: Int](using tileSize: TileSize = TileSize(80))(grid: MapRow[N]*): GameMap =
    GridMap(tileSize.value, grid.toVector)

  export MapCatalog.default4Players as default

  def forPlayers(numPlayers: Int) = numPlayers match
    case 1 => MapCatalog.default1Player
    case 2 => MapCatalog.default2Players
    case 3 => MapCatalog.default3Players
    case 4 => MapCatalog.default4Players
    case _ => throw new IllegalArgumentException(s"Unsupported number of players: $numPlayers")

/** A Dimension represents the width and height of a rectangular area.
 *  @param width  the width of the area
 *  @param height the height of the area
 */
case class Dimension(width: Int, height: Int):
  require(width > 0 && height > 0, "Width and height must be positive integers.")

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
  opaque type MapRow[N <: Int] = Vector[MapTile]

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

  /** A TileSize represents the size of a tile in the game map.
   *  @param value the size of the tile in pixels
   */
  case class TileSize(value: Int)

  object TileSize:
    /** The default tile size used in the game map. */
    val default: TileSize = TileSize(80)

  given Conversion[TileSize, Int] with
    def apply(value: TileSize): Int = value.value

  /** A GridMap represents a game map defined by a grid of MapTiles.
   *  @param tileSize the size of each tile in pixels
   *  @param rows     the rows of MapTiles defining the map layout
   *  @tparam S       the number of tiles in each row
   */
  class GridMap[S <: Int](val tileSize: Int, rows: Vector[MapRow[S]]) extends GameMap:

    val width: Int = rows.head.length * tileSize
    val height: Int = rows.length * tileSize

    /** @inheritdoc */
    val dimension: Dimension = Dimension(width, height)

    /** Builds a GameWorld based on the grid map and a list of player IDs.
     *  @param players the IDs of the players to add to the world
     *  @return the constructed GameWorld
     */
    def buildWorld(settings: GameSettings)(players: Iterable[EntityId]): GameWorld =
      val halfTileSize = tileSize / 2.0 // Needed to place entities at the center of the tiles
      // These offsets center the map around the origin (0,0) in the game world.
      val offsetX = -width / 2.0 + halfTileSize
      val offsetY = -height / 2.0 + halfTileSize
      val tilesWithCords =
        for
          (row, y)  <- rows.zipWithIndex
          (tile, x) <- row.zipWithIndex
        yield (tile, Point2D(offsetX + x * tileSize, offsetY + y * tileSize))

      val spawnPoints = tilesWithCords.collect { case (MapTile.Spawn, point) => point }
      require(
        players.size <= spawnPoints.size,
        s"Cannot build world: ${players.size} players provided, but map only has ${spawnPoints.size} spawn points."
      )
      val walls = tilesWithCords
        .collect { case (MapTile.Wall, point) => point }
        .map(EntityFactory.createWall(_, tileSize, tileSize))
      val mapCenter = Point2D.origin
      val spawnedPlayers = players.zip(spawnPoints).map:
        case (playerId, spawnPoint) =>
          // If the spawn point is at the center of the map, default to a direction pointing upwards (0, 1)
          val direction = if mapCenter != spawnPoint then mapCenter - spawnPoint else Vector2D(0, 1)
          EntityFactory.createSpaceshipFromConfig(settings)(spawnPoint, direction, playerId)
      GameWorld((walls ++ spawnedPlayers).toList)
