package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.model.StatModifier

/** A stat modifier currently affecting a spaceship.
 *
 *  @param modifier  how the stat is modified
 *  @param remaining how long the modification still lasts, in milliseconds
 */
final case class ActiveEffect(modifier: StatModifier, remaining: Long):
  require(remaining > 0L, "An active effect must have some time left")

  /** Lets the given time pass.
   *
   *  @param dt the time to let pass, in milliseconds
   *  @return the effect with less time left, or None if it runs out
   */
  def advanced(dt: Long): Option[ActiveEffect] =
    Option.when(remaining > dt)(copy(remaining = remaining - dt))
