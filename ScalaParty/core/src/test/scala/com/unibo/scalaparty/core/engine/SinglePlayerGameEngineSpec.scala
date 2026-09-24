package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.SystemPipeline
import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.map.{GameMap, SpawnPoint}
import com.unibo.scalaparty.core.model.{ArenaSettings, GameSettings}

val emptyPipeline = SystemPipeline()

class SinglePlayerGameEngineSpec extends GameEngineSpec:

  private val tolerance = 1e-9

  private def spawnedSpaceships(config: GameConfig): Map[EntityId, EntityDto.Spaceship] =
    GameEngine(config)
      .update(Nil, 0L)
      .collect { case spaceship: EntityDto.Spaceship => spaceship.id -> spaceship }
      .toMap

  "A GameEngine with a single player" should "use a SinglePlayerGameEngine" in:
    val player = EntityId.generate()
    val engine = GameEngine(GameConfig.singlePlayer(player, pipeline = emptyPipeline))
    engine shouldBe a[SinglePlayerGameEngine]

  "A SinglePlayerGameEngine" should "spawn only the player at the first spawn point of the map" in:
    val player = EntityId.generate()
    val config = GameConfig.singlePlayer(player, pipeline = emptyPipeline)

    val spaceships = spawnedSpaceships(config)

    spaceships.keySet shouldBe Set(player)
    spaceships(player).position shouldBe config.map.playerSpawns.head.position

  it should "move the spaceship along its spawn heading at the configured speed" in:
    val player = EntityId.generate()
    val map = GameMap(ArenaSettings(), List(SpawnPoint(Point2D(100, 100), heading = 90.0)))
    val config = GameConfig(GameSettings.default, List(player), map, emptyPipeline)

    val velocity = spawnedSpaceships(config)(player).velocity

    velocity.x shouldBe 0.0 +- tolerance
    velocity.y shouldBe config.settings.spaceship.speed +- tolerance

  "A GameConfig" should "reject more players than the map can host" in:
    val players = List.fill(GameMap.default.capacity + 1)(EntityId.generate())
    an[IllegalArgumentException] should be thrownBy GameConfig(GameSettings.default, players)
