package com.unibo.scalaparty.core.model

/** How a match came to its end. */
enum MatchOutcome:
  /** The time limit elapsed before the match was decided otherwise. */
  case TimeUp

  /** Every spaceship was destroyed, so nobody won. */
  case NoSurvivors
