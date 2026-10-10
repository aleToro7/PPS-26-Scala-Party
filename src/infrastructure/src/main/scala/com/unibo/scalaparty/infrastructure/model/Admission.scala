package com.unibo.scalaparty.infrastructure.model

/** Whether the game has taken in a player asking to join it. */
enum Admission:
  /** The player is in, either playing or waiting for its turn. */
  case Admitted

  /** There is no room left for the player, not even in the queue. */
  case Rejected
