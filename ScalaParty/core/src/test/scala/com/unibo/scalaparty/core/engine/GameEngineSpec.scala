package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.ecs.systems.{SystemPipeline, WorldSystem}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.unibo.scalaparty.core.model.GameSettings

class GameEngineSpec extends AnyFlatSpec with Matchers:

  private val emptyPipeline = SystemPipeline()
  private val testSettings = GameSettings.default
  private val someDeltaTime = 100L

  "A GameEngine" should "not create a new world if the pipeline is empty" in:
    val player = EntityId.generate()
    val engine = GameEngine(GameConfig(
      players = List(player),
      settings = testSettings,
      pipeline = emptyPipeline
    ))

    val initialState = engine.update(Nil, 0L)
    val newState = engine.update(Nil, someDeltaTime)

    newState shouldEqual initialState

  it should "update the world state according to the defined pipeline" in:
    val player = EntityId.generate()
    val clearWorldSystem: WorldSystem =
      (world, events, dt) => if dt > 0L then (GameWorld(Nil), events) else (world, events)

    val engine = GameEngine(GameConfig(
      players = List(player),
      settings = testSettings,
      pipeline = SystemPipeline(clearWorldSystem)
    ))

    val initialState = engine.update(Nil, 0L)
    initialState should not be empty

    val newState = engine.update(Nil, someDeltaTime)
    newState shouldBe empty
