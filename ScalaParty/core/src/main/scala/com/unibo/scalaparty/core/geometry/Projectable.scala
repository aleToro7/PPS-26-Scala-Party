package com.unibo.scalaparty.core.geometry

@FunctionalInterface
trait Projectable:
  /** Projects the shape onto the given axis and returns the minimum and maximum projections.
   */
  def projectOnto(axis: Vector2D): (Double, Double)

  /** Checks if there is a separating axis between this shape and another projectable shape.
   *  @param axes the axes to check for separation
   *  @param other the other projectable shape
   *  @return true if there is a separating axis, false otherwise
   */
  def hasSeparatingAxis(axes: Seq[Vector2D])(other: Projectable): Boolean = axes.exists { isSeparatingAxis(_)(other) }

  /** Finds the minimum overlapping axis between this shape and another projectable shape if any.
   *  That is, the axis with the smallest overlap between the two shapes when projected onto that axis.
   *  @param axes the axes to check for separation
   *  @param other the other projectable shape
   *  @return Some((axis, overlap)) if there is an overlapping axis, None otherwise
   */
  def minOverlappingAxis(axes: Seq[Vector2D])(other: Projectable): Option[(Vector2D, Double)] =
    val projections = axes.map: rawAxis =>
      val axis = rawAxis.normalized
      val (min1, max1) = this projectOnto axis
      val (min2, max2) = other projectOnto axis
      val overlap = math.min(max1, max2) - math.max(min1, min2)
      (axis, overlap)
    val (axis, minOverlap) = projections.minBy((_, overlap) => overlap)
    if minOverlap > 0 then Some((axis, minOverlap)) else None

  /** Checks if a given axis is a separating axis between this shape and another projectable shape.
   *  @param axis the axis to check
   *  @param other the other projectable shape
   *  @return true if the axis is a separating axis, false otherwise
   */
  private def isSeparatingAxis(axis: Vector2D)(other: Projectable): Boolean =
    val (min1, max1) = this projectOnto axis
    val (min2, max2) = other projectOnto axis
    max1 < min2 || max2 < min1
