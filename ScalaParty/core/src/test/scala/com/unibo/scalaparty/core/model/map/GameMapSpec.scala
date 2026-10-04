package com.unibo.scalaparty.core.model.map

import com.unibo.scalaparty.core.ecs.{components, EntityId, EntityTypeComponent, MovementComponent}
import com.unibo.scalaparty.core.ecs.EntityType.Wall
import com.unibo.scalaparty.core.geometry.Vector2D
import com.unibo.scalaparty.core.model.GameSettings
import com.unibo.scalaparty.core.model.map.Dsl.{S, W, *}
import com.unibo.scalaparty.core.utils.collectFirstOfClass
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameMapSpec extends AnyFlatSpec with Matchers:

  private val map = GameMap.fromGrid(
    S | / | / | / | / | S,
    S | / | / | / | / | S,
    W | W | W | W | W | W,
  )
  private val numOfWalls = 6
  private val numOfSpawn = 4

  "A GameMap" should "should be able to build a GameWorld" in:
    val players = (1 to numOfSpawn).map(EntityId.fromLong(_))
    val world = map.buildWorld(GameSettings.default)(players)
    world.entities should have size players.size + numOfWalls

  it should "throw an exception if the number of spawn points is less than the number of players" in:
    val map = GameMap.fromGrid(
      S | / | / | / | / | S,
      W | W | W | W | W | W,
    )
    val players = (1 to 4).map(EntityId.fromLong(_))
    an[IllegalArgumentException] should be thrownBy:
      map.buildWorld(GameSettings.default)(players)

  it should "not throw any exception if spawns are more than players" in:
    val players = EntityId.generate() :: Nil
    val world = map.buildWorld(GameSettings.default)(players)
    world.entities should have size players.size + numOfWalls

  it should "create the correct number of walls" in:
    val players = (1 to numOfSpawn).map(EntityId.fromLong(_))
    val world = map.buildWorld(GameSettings.default)(players)
    val wallCount = world.entitiesWithComponents.flatMap(
      _.components.collectFirstOfClass[EntityTypeComponent]
    ).count(_.entityType == Wall)
    wallCount shouldBe numOfWalls

  it should "not zero the entities' velocity when placing them at the origin" in:
    val player = EntityId.generate()
    val map = GameMap.fromGrid(/ | S | /)
    val world = map.buildWorld(GameSettings.default)(List(player))
    val playerVelocity = world.findComponents(player).flatMap:
      _.collectFirstOfClass[MovementComponent].map(_.velocity)
    playerVelocity.value should not be Vector2D.zero
