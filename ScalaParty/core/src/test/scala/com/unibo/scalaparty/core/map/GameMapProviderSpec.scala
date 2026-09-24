package com.unibo.scalaparty.core.map

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameMapProviderSpec extends AnyWordSpec with Matchers:

  private val map = GameMap.default
  private val provider = GameMapProvider.fixed(map)

  "A fixed GameMapProvider" should:

    "provide its map for any number of players the map can host" in:
      (1 to map.capacity).foreach: players =>
        provider.mapFor(players) shouldBe Some(map)

    "provide no map for more players than the map can host" in:
      provider.mapFor(map.capacity + 1) shouldBe None

    "reject a request for a non-positive number of players" in:
      List(0, -1).foreach: players =>
        an[IllegalArgumentException] should be thrownBy provider.mapFor(players)
