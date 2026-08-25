package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId

class SinglePlayerGameEngineSpec extends GameEngineSpec:

  "A GameEngine with a single player" should "use a SinglePlayerGameEngine" in :
    val player = EntityId.generate()
    val engine = GameEngine(player)
    engine shouldBe a[SinglePlayerGameEngine]

  "A SinglePlayerGameEngine" should "place the player in the center of the world" in:
    val player = EntityId.generate()
    val engine = GameEngine(player)
    val state = engine.update(Nil, 0)