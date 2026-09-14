package com.unibo.scalaparty.infrastructure.model

import java.util.UUID

/** Opaque type representing a unique identifier for a player, wrapping a Java UUID
 *  to provide compile-time type safety with zero runtime overhead.
 */
opaque type PlayerId = UUID

object PlayerId:

  /** Generates a new random player identifier.
   *
   * @return a newly created random [[PlayerId]]
   */
  def random(): PlayerId = UUID.randomUUID()

  /** Parses a player identifier from its string representation.
   *
   * @param s the string representation of the UUID
   * @return the corresponding [[PlayerId]]
   */
  def fromString(s: String): PlayerId = UUID.fromString(s)

  extension (id: PlayerId)

    /** Extracts the underlying Java UUID value from the player identifier.
     *
     *  @return the raw [[UUID]] wrapped by this player ID
     */
    def value: UUID = id
