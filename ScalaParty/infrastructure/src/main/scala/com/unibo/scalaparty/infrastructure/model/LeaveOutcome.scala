package com.unibo.scalaparty.infrastructure.model

/** What the departure of a player does to the matches being played.
 *
 *  @param disbanded The match the player was the last one left in, which is therefore over.
 *  @param started   The match begun in the room that departure has freed, if anybody was waiting.
 */
final case class LeaveOutcome(disbanded: Option[MatchId], started: Option[ActiveMatch])
