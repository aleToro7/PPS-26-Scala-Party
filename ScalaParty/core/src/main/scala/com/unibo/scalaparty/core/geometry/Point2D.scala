package com.unibo.scalaparty.core.geometry

/**
 * Represents a two-dimensional point and provides operations to manipulate points.
 *
 * @param x the X coordinate of the point
 * @param y the Y coordinate of the point
 */
final case class Point2D(x: Double, y: Double):

  /**
   * Computes the new point resulting from the sum of this point and a given vector.
   *
   *
   * @param v the vector to add to this point
   * @return a new [[Point2D]] representing the translated point
   */
  def +(v: Vector2D): Point2D = Point2D(x + v.x, y + v.y)

  /**
   * Computes the vector from this point to another point.
   *
   * @param p the other point
   * @return a [[Vector2D]] representing the displacement from this point to the other point
   */
  def -(p: Point2D): Vector2D = Vector2D(x - p.x, y - p.y)

object Point2D:
  /** A constant point representing the origin `(0.0, 0.0)`. */
  val origin: Point2D = Point2D(0.0, 0.0)
