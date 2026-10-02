package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.ecs.EntityId

/** How a match came to its end. */
enum MatchOutcome:
  /** The time limit elapsed before the match was decided otherwise. */
  case TimeUp

  /** Every spaceship was destroyed, so nobody won. */
  case NoSurvivors

  /** Only the given spaceship is left, so its player won. */
  case LastStanding(winner: EntityId)
