package com.unibo.scalaparty.infrastructure.model

/** A match being played, together with the players pulled out of the waiting queue to take part in
 *  it. Players are chosen when the match begins and the roster never changes afterwards: whoever
 *  arrives later waits for the next one.
 *
 *  @param matchId The identifier of the match.
 *  @param players The players taking part in it.
 */
final case class ActiveMatch(matchId: MatchId, players: Set[PlayerId])
