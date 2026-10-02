package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchRulesSpec extends AnyFlatSpec with Matchers:

  private val timeLimit = 1_000L
  private val settings = MatchSettings(timeLimit)
  private val survivor = EntityId.generate()

  "MatchRules" should "keep a match going before its time limit" in:
    MatchRules.outcome(settings, Set(survivor), elapsed = timeLimit - 1) shouldBe None

  it should "end a match once its time limit is reached" in:
    MatchRules.outcome(settings, Set(survivor), elapsed = timeLimit) shouldBe Some(MatchOutcome.TimeUp)

  it should "end a match once no spaceship is left" in:
    MatchRules.outcome(settings, Set.empty, elapsed = 0L) shouldBe Some(MatchOutcome.NoSurvivors)

  it should "tell that no spaceship is left even when the time limit is reached too" in:
    MatchRules.outcome(settings, Set.empty, elapsed = timeLimit) shouldBe Some(MatchOutcome.NoSurvivors)
