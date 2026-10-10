package com.unibo.scalaparty.core.geometry

/** Represents a two-dimensional point and provides operations to manipulate points.
 *
 *  @param x the X coordinate of the point
 *  @param y the Y coordinate of the point
 */
final case class Point2D(x: Double, y: Double):

  /** Computes the new point resulting from the sum of this point and a given vector.
   *
   *  @param v the vector to add to this point
   *  @return a new [[Point2D]] representing the translated point
   */
  def +(v: Vector2D): Point2D = Point2D(x + v.x, y + v.y)

  /** Computes the vector from this point to another point.
   *
   *  @param p the other point
   *  @return a [[Vector2D]] representing the displacement from this point to the other point
   */
  def -(p: Point2D): Vector2D = Vector2D(x - p.x, y - p.y)

  /** Computes the Euclidean distance between this point and another point.
   *
   *  @param other the other point
   *  @return the distance as a [[Double]]
   */
  def distanceTo(other: Point2D): Double = (this - other).module

  /** Computes the squared Euclidean distance between this point and another point.
   *  Useful to avoid square root calculations when comparing distances.
   *
   *  @param other the other point
   *  @return the squared distance as a [[Double]]
   */
  def distanceSquaredTo(other: Point2D): Double = (this - other).moduleSquared

  /** Converts this point to a [[Vector2D]] representing the position vector from origin. */
  def toVector: Vector2D = Vector2D(x, y)

object Point2D:
  /** A constant point representing the origin `(0.0, 0.0)`. */
  val origin: Point2D = Point2D(0.0, 0.0)

  /** Creates a point from a tuple of coordinates. */
  def apply(t: (Double, Double)): Point2D = Point2D(t._1, t._2)

  given Conversion[(Double, Double), Point2D] with
    def apply(t: (Double, Double)): Point2D = Point2D(t._1, t._2)

export Point2D.given
