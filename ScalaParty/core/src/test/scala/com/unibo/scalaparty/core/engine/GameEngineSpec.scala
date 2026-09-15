package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.ecs.systems.{System, SystemPipeline}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.unibo.scalaparty.core.model.GameSettings

class GameEngineSpec extends AnyFlatSpec with Matchers:

  private val emptyPipeline = SystemPipeline()
  private val testSettings = GameSettings(
    worldWidth = 800,
    worldHeight = 600,
    spaceshipSpeed = 5.0,
    spaceshipRotationSpeed = 0.5
  )

  "A GameEngine" should "not create a new world if the pipeline is empty" in:
    val player = EntityId.generate()
    val someTime = 100L // 100 milliseconds
    val engine = GameEngine(GameConfig(
      players = List(player),
      settings = testSettings,
      pipeline = emptyPipeline
    ))
    val initialState = engine.update(Nil, 0)
    val newState = engine.update(Nil, someTime)
    newState shouldEqual initialState

  "A GameEngine" should "update the world state according to the defined pipeline" in:
    val player = EntityId.generate()
    val clearWorldSystem: System = (world, events, dt) => if dt > 0 then (GameWorld(Nil), events) else (world, events)
    val someTime = 100L // 100 milliseconds
    val engine = GameEngine(GameConfig(
      players = List(player),
      settings = testSettings,
      pipeline = SystemPipeline(clearWorldSystem)
    ))
    val initialState = engine.update(Nil, 0)
    initialState should not be empty
    val newState = engine.update(Nil, someTime)
    newState shouldBe empty
