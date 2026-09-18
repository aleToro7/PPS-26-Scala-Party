package com.unibo.scalaparty.infrastructure.model

/** The lifecycle phase of a match, from lobby formation to completion.
 */
enum MatchStatus:
  /** The lobby is open and still admits new players. */
  case Waiting

  /** The match is being played and no longer admits players. */
  case Running

  /** The match is over. */
  case Finished
