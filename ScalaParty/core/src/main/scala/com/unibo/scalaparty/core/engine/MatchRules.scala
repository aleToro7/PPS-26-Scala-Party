package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}

/** The rules deciding when a match is over and how it ended.
 *
 *  A match ending by elimination is reported as such even when its time limit is reached in the same tick.
 */
object MatchRules:

  /** Tells how a match ended, if it did.
   *
   *  @param settings  the rules of the match
   *  @param survivors the spaceships of the players still in the match, the others having been destroyed or left
   *  @param elapsed   the simulated time since the match began, in milliseconds
   *  @return how the match ended, or `None` while it is still going on
   */
  def outcome(settings: MatchSettings, survivors: Set[EntityId], elapsed: Long): Option[MatchOutcome] =
    if survivors.isEmpty then Some(MatchOutcome.NoSurvivors)
    else Option.when(elapsed >= settings.timeLimit)(MatchOutcome.TimeUp)
