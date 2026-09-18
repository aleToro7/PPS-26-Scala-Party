package com.unibo.scalaparty.infrastructure.model

/** A notification sent by the server to a single player about its place in the game.
 *
 *  These messages concern the lobby, not the gameplay: the authoritative match state travels
 *  separately through the match event publisher.
 */
enum ServerMessage:
  /** The player is waiting, with the given number of players to be served before it. */
  case Queued(playersAhead: Int)

  /** A match the player takes part in has just begun, with the given number of participants. */
  case MatchStarted(players: Int)

  /** The match the player was taking part in is over. */
  case MatchEnded
