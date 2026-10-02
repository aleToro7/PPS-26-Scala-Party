package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchRulesSpec extends AnyFlatSpec with Matchers:

  private val timeLimit = 1_000L
  private val settings = MatchSettings(timeLimit)

  "MatchRules" should "keep a match going before its time limit" in:
    MatchRules.outcome(settings, elapsed = timeLimit - 1) shouldBe None

  it should "end a match once its time limit is reached" in:
    MatchRules.outcome(settings, elapsed = timeLimit) shouldBe Some(MatchOutcome.TimeUp)
