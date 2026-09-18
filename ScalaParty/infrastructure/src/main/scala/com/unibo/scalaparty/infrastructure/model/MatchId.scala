package com.unibo.scalaparty.infrastructure.model

import java.util.UUID

/** Opaque type representing a unique identifier for a match, wrapping a Java UUID
 *  to provide compile-time type safety with zero runtime overhead.
 */
opaque type MatchId = UUID

object MatchId:

  /** Generates a new random match identifier.
   *
   *  @return a newly created random [[MatchId]]
   */
  def random(): MatchId = UUID.randomUUID()

  /** Parses a match identifier from its string representation.
   *
   *  @param s the string representation of the UUID
   *  @return the corresponding [[MatchId]]
   */
  def fromString(s: String): MatchId = UUID.fromString(s)

  extension (id: MatchId)

    /** Extracts the underlying Java UUID value from the match identifier.
     *
     *  @return the raw [[UUID]] wrapped by this match ID
     */
    def value: UUID = id
