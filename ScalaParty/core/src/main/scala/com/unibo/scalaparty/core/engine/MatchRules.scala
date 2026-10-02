package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}

/** The rules deciding when a match is over and how it ended. */
object MatchRules:

  /** Tells how a match ended, if it did.
   *
   *  @param settings the rules of the match
   *  @param elapsed  the simulated time since the match began, in milliseconds
   *  @return how the match ended, or `None` while it is still going on
   */
  def outcome(settings: MatchSettings, elapsed: Long): Option[MatchOutcome] =
    Option.when(elapsed >= settings.timeLimit)(MatchOutcome.TimeUp)
