package com.unibo.scalaparty.core.geometry

/** Represents a 1D scalar interval resulting from projecting a 2D geometric shape onto an axis.
 *
 *  @param min the minimum coordinate on the projected axis
 *  @param max the maximum coordinate on the projected axis
 */
final case class Projection(min: Double, max: Double):

  /** Checks if this 1D projection overlaps with another projection.
   *  Two projections overlap if they share at least one point (border touching is considered overlapping).
   *
   *  @param other the other projection
   *  @return true if they overlap, false otherwise
   */
  def overlaps(other: Projection): Boolean =
    this.max >= other.min && other.max >= this.min

  /** Computes the amount of overlap between this projection and another.
   *  If the projections do not overlap, the returned value is negative or zero.
   *
   *  @param other the other projection
   *  @return the overlap amount as a [[Double]]
   */
  def overlap(other: Projection): Double =
    math.min(this.max, other.max) - math.max(this.min, other.min)
