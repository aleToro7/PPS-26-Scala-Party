package com.unibo.scalaparty.infrastructure.model

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.MatchOutcome

/** A notification sent by the server to a single player about its place in the game.
 *
 *  These messages concern the lobby, not the gameplay: the authoritative match state travels
 *  separately through the match event publisher.
 */
enum ServerMessage:
  /** The player is waiting, with the given number of players to be served before it. */
  case Queued(playersAhead: Int)

  /** The player has been turned away: every room is taken and the queue is full. */
  case QueueFull

  /** A match the player takes part in has just begun, with the given number of participants.
   *
   *  `you` is the entity the player controls in it: the spaceship carrying that id in the match state
   *  is its own.
   */
  case MatchStarted(players: Int, you: EntityId)

  /** The match the player was taking part in is over, for the given reason. */
  case MatchEnded(outcome: MatchOutcome)
