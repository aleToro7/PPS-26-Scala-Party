package com.unibo.scalaparty.core.geometry

/** Represents a two-dimensional vector and provides standard vector operations.
 *
 * @param x the X coordinate of the vector
 * @param y the Y coordinate of the vector
 */
final case class Vector2D(x: Double, y: Double):

  /** Adds another vector to this vector component-wise.
   *
   * @param other the vector to add
   * @return a new [[Vector2D]] representing the vector sum
   */
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)

  /** Subtracts another vector from this one.
   *
   * @param other the vector to subtract
   * @return a new [[Vector2D]] representing the vector difference
   */
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)

  /** Multiplies this vector by a scalar value.
   *
   * @param scalar the scaling factor
   * @return a new [[Vector2D]] scaled by the given factor
   */
  def *(scalar: Double): Vector2D =
    Vector2D(this.x * scalar, this.y * scalar)

  /** Computes the magnitude (length) of this vector.
   *
   * @return the length of the vector as a [[Double]]
   */
  def module: Double =
    Math.hypot(x, y)

  /** Computes a normalized (unit) vector pointing in the same direction as this vector.
   *
   * If the vector magnitude is `0.0`, returns [[Vector2D.zero]] to avoid division by zero.
   *
   * @return a unit [[Vector2D]] with a magnitude of `1.0`, or [[Vector2D.zero]] if magnitude is `0.0`
   */
  def normalized: Vector2D =
    val mod = module
    if mod == 0.0 then Vector2D.zero else Vector2D(x / mod, y / mod)

  /** Rotates this vector by a specified angle in degrees.
   * The rotation is counter-clockwise in the Cartesian plane.
   *
   * @param angleDegrees the angle in degrees to rotate the vector
   * @return a new [[Vector2D]] representing the rotated vector
   */
  def rotated(angleDegrees: Double): Vector2D =
    val angleRadians = Math.toRadians(angleDegrees)
    val cosTheta = Math.cos(angleRadians)
    val sinTheta = Math.sin(angleRadians)
    Vector2D(
      x * cosTheta - y * sinTheta, // x' = r * cos(alpha + theta) --> expand
      x * sinTheta + y * cosTheta // y' = r * sin(alpha + theta) --> expand
    )

object Vector2D:
  /** A constant vector representing the origin `(0.0, 0.0)`. */
  val zero: Vector2D = Vector2D(0.0, 0.0)
