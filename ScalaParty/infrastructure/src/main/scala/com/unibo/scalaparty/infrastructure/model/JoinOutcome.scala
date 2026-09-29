package com.unibo.scalaparty.infrastructure.model

/** What becomes of a player asking to take part in the game.
 */
enum JoinOutcome:
  /** The player takes part in the match that is now being played. */
  case Playing(activeMatch: ActiveMatch)

  /** The player waits for its turn, with the given number of players to be served before it. */
  case Queued(playersAhead: Int)

  /** The player is turned away, the queue being full. */
  case Rejected
