package com.unibo.scalaparty.core.ecs

import java.util.concurrent.atomic.AtomicLong

/** Represents a unique identifier for an entity of the World. */
opaque type EntityId = Long

object EntityId:
  private val counter = new AtomicLong()

  /** Generates a new unique [[EntityId]].
   *
   * @return a new unique [[EntityId]]
   */
  def generate(): EntityId = counter.getAndIncrement()

  /** Explicit boundary method for external infrastructure (e.g., deserialization).
   * Use with caution outside of network codecs.
   */
  def fromLong(value: Long): EntityId = value

  extension (id: EntityId)
    /** Retrieves the underlying Long value of the [[EntityId]].
     *
     * @return the Long value representing the [[EntityId]]
     */
    def value: Long = id

