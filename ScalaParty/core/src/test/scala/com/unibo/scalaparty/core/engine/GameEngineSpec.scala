package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.ecs.systems.{SystemPipeline, WorldSystem}
import com.unibo.scalaparty.core.model.{GameSettings, MatchOutcome, MatchSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameEngineSpec extends AnyFlatSpec with Matchers:

  private val emptyPipeline = SystemPipeline()
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
    val initialState = engine.update(Nil, 0L)
    val newState = engine.update(Nil, someDeltaTime)
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
      SystemPipeline(clearWorldSystem)
    )
    val initialState = engine.update(Nil, 0L)
    initialState should not be empty
    val newState = engine.update(Nil, someDeltaTime)
    newState shouldBe empty

  it should "not be over before its time limit has elapsed" in:
    val engine = engineWithTimeLimit(2 * someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe None

  it should "be over once the simulated time reaches its time limit" in:
    val engine = engineWithTimeLimit(2 * someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe Some(MatchOutcome.TimeUp)

  private def engineWithTimeLimit(timeLimit: Long): GameEngine =
    GameEngine(
      GameConfig(
        players = List(EntityId.generate()),
        settings = testSettings.copy(matchSettings = MatchSettings(timeLimit))
      ),
      emptyPipeline
    )
