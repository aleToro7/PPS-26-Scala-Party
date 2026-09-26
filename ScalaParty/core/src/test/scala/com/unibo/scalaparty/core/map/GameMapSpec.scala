package com.unibo.scalaparty.core.map

import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.model.ArenaSettings
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameMapSpec extends AnyWordSpec with Matchers:

  private val arena = ArenaSettings(width = 800, height = 600)
  private val spawns = List(
    SpawnPoint(Point2D(100, 100), heading = 0.0),
    SpawnPoint(Point2D(700, 500), heading = 180.0)
  )

  "SpawnPoint" should:

    "face the given target" in:
      val origin = Point2D(100, 100)
      val headingTowards = (target: Point2D) => SpawnPoint.facing(origin, target).heading

      headingTowards(Point2D(200, 100)) shouldBe 0.0
      headingTowards(Point2D(100, 200)) shouldBe 90.0
      headingTowards(Point2D(0, 100)) shouldBe 180.0
      headingTowards(Point2D(100, 0)) shouldBe -90.0

  "The default GameMap" should:

    val default = GameMap.default
    val center = Point2D(default.arena.width / 2.0, default.arena.height / 2.0)

    "host up to four players" in:
      default.capacity shouldBe 4

    "place every spawn point facing the center of the arena" in:
      default.playerSpawns.foreach: spawn =>
        spawn shouldBe SpawnPoint.facing(spawn.position, center)

    "place consecutive pairs of spawn points on opposite sides of the center" in:
      default.playerSpawns.grouped(2).foreach:
        case List(first, second) => (first.position - center) shouldBe (center - second.position)
        case _ => fail("The default map should define an even number of spawn points")

  "GameMap" should:

    "expose a capacity equal to the number of player spawn points" in:
      GameMap(arena, spawns).capacity shouldBe spawns.size

    "reject a map without player spawn points" in:
      an[IllegalArgumentException] should be thrownBy GameMap(arena, Nil)

    "reject player spawn points outside the arena" in:
      val outside = List(Point2D(-1, 100), Point2D(801, 100), Point2D(100, -1), Point2D(100, 601))
      outside.foreach: position =>
        an[IllegalArgumentException] should be thrownBy GameMap(arena, List(SpawnPoint(position, 0.0)))

    "accept player spawn points lying on the arena edges" in:
      val corners = List(Point2D(0, 0), Point2D(800, 600))
      noException should be thrownBy GameMap(arena, corners.map(SpawnPoint(_, 0.0)))
