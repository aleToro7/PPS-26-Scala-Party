package com.unibo.scalaparty.core.geometry

@FunctionalInterface
trait Projectable:
  /**Projects the shape onto the given axis and returns the minimum and maximum projections.
   */
  def projectOnto(axis: Vector2D): (Double, Double)
  
  /**Checks if there is a separating axis between this shape and another projectable shape.
   * @param axes the axes to check for separation
   * @param other the other projectable shape
   * @return true if there is a separating axis, false otherwise
   */
  def hasSeparatingAxis(axes: Seq[Vector2D])(other: Projectable): Boolean =
    axes.exists: axis =>
      val (min1, max1) = this projectOnto axis
      val (min2, max2) = other projectOnto axis
      max1 < min2 || max2 < min1
