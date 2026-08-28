package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.SystemPipeline

val emptyPipeline = SystemPipeline()

class SinglePlayerGameEngineSpec extends GameEngineSpec:

  "A GameEngine with a single player" should "use a SinglePlayerGameEngine" in:
    val player = EntityId.generate()
    val engine = GameEngine(GameConfig.singlePlayer(player, pipeline = emptyPipeline))
    engine shouldBe a[SinglePlayerGameEngine]

  "A SinglePlayerGameEngine" should "place the player in the center of the world" in:
    val player = EntityId.generate()
    val engine = GameEngine(GameConfig.singlePlayer(player, pipeline = emptyPipeline))
    val state = engine.update(Nil, 0)
