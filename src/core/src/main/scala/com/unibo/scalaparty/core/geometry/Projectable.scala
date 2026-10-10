package com.unibo.scalaparty.core.geometry

/** Represents an entity that can be projected onto a 2D axis for collision detection
 *  using the Separating Axis Theorem (SAT).
 */
@FunctionalInterface
trait Projectable:

  /** Projects the shape onto the given axis and returns the 1D projection interval.
   *
   *  @param axis the vector axis to project onto
   *  @return the [[Projection]] interval containing minimum and maximum projected values
   */
  def projectOnto(axis: Vector2D): Projection

  /** Checks if there is a separating axis between this shape and another projectable shape.
   *  According to the Separating Axis Theorem (SAT), if there exists an axis along which
   *  the projections do not overlap, the shapes are separated and cannot intersect.
   *
   *  @param axes the candidate axes to check for separation
   *  @param other the other projectable shape
   *  @return true if there is a separating axis, false otherwise
   */
  def hasSeparatingAxis(axes: Seq[Vector2D])(other: Projectable): Boolean =
    axes.exists(isSeparatingAxis(_)(other))

  /** Finds the minimum overlapping axis between this shape and another projectable shape if any.
   *  That is, the axis with the smallest positive overlap between the two shapes when projected onto that axis.
   *
   *  @param axes the candidate axes to check for separation
   *  @param other the other projectable shape
   *  @return Some((axis, overlap)) if all axes overlap and overlap > 0, None otherwise
   */
  def minOverlappingAxis(axes: Seq[Vector2D])(other: Projectable): Option[(Vector2D, Double)] =
    if axes.isEmpty then None
    else
      val projections = axes.map: rawAxis =>
        val axis = rawAxis.normalized
        val proj1 = this.projectOnto(axis)
        val proj2 = other.projectOnto(axis)
        (axis, proj1.overlap(proj2))

      val (minAxis, minOverlap) = projections.minBy((_, overlap) => overlap)
      if minOverlap > 0 then Some((minAxis, minOverlap)) else None

  /** Checks if a given axis is a separating axis between this shape and another projectable shape.
   *
   *  @param axis the axis to check
   *  @param other the other projectable shape
   *  @return true if the axis is a separating axis, false otherwise
   */
  private def isSeparatingAxis(axis: Vector2D)(other: Projectable): Boolean =
    val proj1 = this.projectOnto(axis)
    val proj2 = other.projectOnto(axis)
    !proj1.overlaps(proj2)
