package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.{EntityId, GameEvent, GameWorld}
import com.unibo.scalaparty.core.ecs.systems.WorldSystem
import com.unibo.scalaparty.core.model.GameSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameEngineSpec extends AnyFlatSpec with Matchers:

  private val emptyPipeline: WorldSystem = (world, events, _) => (world, events)
  private val testSettings = GameSettings.default
  private val someDeltaTime = 100L

  "A GameEngine" should "not create a new world if the pipeline is empty" in:
    val player = EntityId.generate()
    val engine = GameEngine(
      GameConfig(
        players = List(player),
        settings = testSettings
      ),
      emptyPipeline
    )
    val initialState = engine.update(Nil, 0L).entities
    val newState = engine.update(Nil, someDeltaTime).entities
    newState shouldEqual initialState

  it should "update the world state according to the defined pipeline" in:
    val player = EntityId.generate()
    val clearWorldSystem: WorldSystem =
      (world, events, dt) => if dt > 0L then (GameWorld(Nil), events) else (world, events)
    val engine = GameEngine(
      GameConfig(
        players = List(player),
        settings = testSettings,
      ),
      clearWorldSystem
    )
    val initialState = engine.update(Nil, 0L).entities
    initialState should not be empty
    val newState = engine.update(Nil, someDeltaTime).entities
    newState shouldBe empty

  it should "return the events occurred during the update" in:
    val player = EntityId.generate()
    val announceDeath: WorldSystem = (world, events, _) => (world, events + GameEvent.Death(player))
    val engine = GameEngine(
      GameConfig(
        players = List(player),
        settings = testSettings
      ),
      announceDeath
    )
    engine.update(Nil, someDeltaTime).events shouldBe Set(GameEvent.Death(player))
