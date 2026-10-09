package com.unibo.scalaparty.core.ecs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchClockComponentSpec extends AnyFlatSpec with Matchers:

  "MatchClockComponent" should "start at the beginning of the match by default" in:
    MatchClockComponent().elapsed shouldBe 0L

  it should "add the given time to its elapsed time when advanced" in:
    MatchClockComponent(100L).advanced(16L).elapsed shouldBe 116L

  it should "reject a negative time to advance by" in:
    an[IllegalArgumentException] should be thrownBy MatchClockComponent().advanced(-1L)

  it should "reject a negative elapsed time" in:
    an[IllegalArgumentException] should be thrownBy MatchClockComponent(-1L)
