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
