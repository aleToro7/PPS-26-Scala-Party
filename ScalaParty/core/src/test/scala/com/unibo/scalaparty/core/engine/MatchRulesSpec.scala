package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchRulesSpec extends AnyFlatSpec with Matchers:

  private val timeLimit = 1_000L
  private val settings = MatchSettings(timeLimit)
  private val first = EntityId.generate()
  private val second = EntityId.generate()
  private val third = EntityId.generate()
  private val players = Set(first, second, third)

  private def outcome(players: Set[EntityId], survivors: Set[EntityId], elapsed: Long = 0L): Option[MatchOutcome] =
    MatchRules.outcome(settings, players, survivors, elapsed)

  "MatchRules" should "keep a match going before its time limit" in:
    outcome(players, survivors = players, elapsed = timeLimit - 1) shouldBe None

  it should "end a match once its time limit is reached" in:
    outcome(players, survivors = players, elapsed = timeLimit) shouldBe Some(MatchOutcome.TimeUp)

  it should "end a match once no spaceship is left" in:
    outcome(players, survivors = Set.empty) shouldBe Some(MatchOutcome.NoSurvivors)

  it should "tell that no spaceship is left even when the time limit is reached too" in:
    outcome(players, survivors = Set.empty, elapsed = timeLimit) shouldBe Some(MatchOutcome.NoSurvivors)

  it should "keep a match going while several spaceships are left" in:
    outcome(players, survivors = Set(first, second)) shouldBe None

  it should "declare the last spaceship left the winner" in:
    outcome(players, survivors = Set(first)) shouldBe Some(MatchOutcome.LastStanding(first))

  it should "declare the winner even when the time limit is reached too" in:
    outcome(players, survivors = Set(first), elapsed = timeLimit) shouldBe Some(MatchOutcome.LastStanding(first))

  it should "keep a match started by a single player going while its spaceship is alive" in:
    outcome(players = Set(first), survivors = Set(first)) shouldBe None
