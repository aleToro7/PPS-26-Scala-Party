package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.{EntityId, GameEvent, GameWorld}
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
      SystemPipeline(clearWorldSystem)
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
      SystemPipeline(announceDeath)
    )
    engine.update(Nil, someDeltaTime).events shouldBe Set(GameEvent.Death(player))

  it should "not be over before its time limit has elapsed" in:
    val engine = engineWithTimeLimit(2 * someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe None

  it should "be over once the simulated time reaches its time limit" in:
    val engine = engineWithTimeLimit(2 * someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe Some(MatchOutcome.TimeUp)

  it should "be over with no survivors once the spaceships of its players are gone" in:
    val destroyEverything: WorldSystem = (_, events, _) => (GameWorld(Nil), events)
    val engine = GameEngine(
      GameConfig(
        players = List(EntityId.generate(), EntityId.generate()),
        settings = testSettings
      ),
      SystemPipeline(destroyEverything)
    )
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe Some(MatchOutcome.NoSurvivors)

  it should "be won by the player whose spaceship is the last one left" in:
    val winner = EntityId.generate()
    val loser = EntityId.generate()
    val destroyLoser: WorldSystem = (world, events, _) => (world - loser, events)
    val engine = GameEngine(
      GameConfig(
        players = List(winner, loser),
        settings = testSettings
      ),
      SystemPipeline(destroyLoser)
    )
    engine.update(Nil, someDeltaTime)
    engine.outcome shouldBe Some(MatchOutcome.LastStanding(winner))

  private def engineWithTimeLimit(timeLimit: Long): GameEngine =
    GameEngine(
      GameConfig(
        players = List(EntityId.generate()),
        settings = testSettings.copy(matchSettings = MatchSettings(timeLimit))
      ),
      emptyPipeline
    )
