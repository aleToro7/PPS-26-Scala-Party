package com.unibo.scalaparty.core.model.map

import com.unibo.scalaparty.core.model.map.Dsl.*
import org.scalatest.Assertions.assertCompiles
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DslSpec extends AnyFlatSpec with Matchers:

  "Dsl" should "consent to define a GameMap using dedicated syntax" in:
    assertCompiles("GameMap.fromGrid(" +
      "S | * | * | W | * | * | S," +
      "* | W | * | W | * | W | *," +
      "* | W | * | * | * | W | *," +
      "* | W | W | W | W | W | *," +
      "S | * | * | W | * | * | S" +
      ")")

  it should "validate that all rows have the same length at compile time" in:
    assertDoesNotCompile("GameMap.fromGrid(" +
      "S | * | * | W | * | * | S," +
      "* | W | * | * | * | W | *," +
      "* | W | * | W | * | W," + // This row has 6 tiles instead of 7
      "* | W | W | W | W | W | *," +
      "S | * | * | W | * | * | S" +
      ")")

  it should "return a GameMap with the correct dimensions" in:
    given tileSize: TileSize = TileSize(32)
    val cols = 7
    val rows = 5
    val map = GameMap.fromGrid(
      S | * | * | W | * | * | S,
      * | W | * | W | * | W | *,
      * | W | * | * | * | W | *,
      * | W | W | W | W | W | *,
      S | * | * | W | * | * | S
    )
    map.dimension.width shouldBe cols * tileSize
    map.dimension.height shouldBe rows * tileSize
