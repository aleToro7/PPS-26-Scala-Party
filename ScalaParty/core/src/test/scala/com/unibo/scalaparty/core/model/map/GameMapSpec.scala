package com.unibo.scalaparty.core.model.map

import com.unibo.scalaparty.core.ecs.{
  components,
  EntityId,
  EntityTypeComponent,
  GameWorld,
  MovementComponent,
  PositionComponent,
  PowerUpSpotComponent
}
import com.unibo.scalaparty.core.ecs.EntityType.Wall
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.{GameSettings, PowerUpSettings}
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

  private val powerUpMap = GameMap.fromGrid(S | P | / | P | S)

  private def spotsOf(world: GameWorld): List[(Point2D, PowerUpSpotComponent)] =
    for
      (_, components) <- world.findEntitiesWithComponent[PowerUpSpotComponent]
      position        <- components.collectFirstOfClass[PositionComponent]
      spot            <- components.collectFirstOfClass[PowerUpSpotComponent]
    yield (position.position, spot)

  private def spotsBuiltWithSeed(seed: Long): List[PowerUpSpotComponent] =
    val settings = GameSettings.default.copy(powerUps = PowerUpSettings(seed = seed))
    spotsOf(powerUpMap.buildWorld(settings)(Nil)).map((_, spot) => spot)

  it should "create a power-up spot at the center of every power-up tile" in:
    val world = powerUpMap.buildWorld(GameSettings.default)(Nil)
    spotsOf(world).map((position, _) => position) should contain theSameElementsAs List(
      Point2D(-80.0, 0.0),
      Point2D(80.0, 0.0)
    )

  it should "give every power-up spot its own generator" in:
    val generators = spotsBuiltWithSeed(0L).map(_.random)
    generators.distinct should have size generators.size

  it should "draw the power-ups of its spots from the seed in the settings" in:
    spotsBuiltWithSeed(42L) shouldBe spotsBuiltWithSeed(42L)
    spotsBuiltWithSeed(42L).map(_.random) should not equal spotsBuiltWithSeed(7L).map(_.random)

  it should "not create any power-up spot without power-up tiles" in:
    val world = map.buildWorld(GameSettings.default)(Nil)
    spotsOf(world) shouldBe empty
