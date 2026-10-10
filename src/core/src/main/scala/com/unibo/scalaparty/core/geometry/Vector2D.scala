package com.unibo.scalaparty.core.geometry

/** Represents a two-dimensional vector and provides standard vector operations.
 *
 *  @param x the X coordinate of the vector
 *  @param y the Y coordinate of the vector
 */
final case class Vector2D(x: Double, y: Double):

  /** Adds another vector to this vector component-wise.
   *
   *  @param other the vector to add
   *  @return a new [[Vector2D]] representing the vector sum
   */
  def +(other: Vector2D): Vector2D =
    Vector2D(this.x + other.x, this.y + other.y)

  /** Subtracts another vector from this one.
   *
   *  @param other the vector to subtract
   *  @return a new [[Vector2D]] representing the vector difference
   */
  def -(other: Vector2D): Vector2D =
    Vector2D(this.x - other.x, this.y - other.y)

  /** Negates this vector, effectively reversing its direction.
   *  @return a new [[Vector2D]] pointing in the opposite direction
   */
  def unary_- : Vector2D =
    Vector2D(-this.x, -this.y)

  /** Multiplies this vector by a scalar value.
   *
   *  @param scalar the scaling factor
   *  @return a new [[Vector2D]] scaled by the given factor
   */
  def *(scalar: Double): Vector2D =
    Vector2D(this.x * scalar, this.y * scalar)

  /** Computes the scalar product of this vector and another vector.
   *
   *  @param other the other vector
   *  @return the dot product as a [[Double]]
   */
  def dot(other: Vector2D): Double =
    this.x * other.x + this.y * other.y

  /** Computes the 2D cross product of this vector and another vector.
   *
   *  @param other the other vector
   *  @return the cross product as a [[Double]]
   */
  def cross(other: Vector2D): Double =
    this.x * other.y - this.y * other.x

  /** Computes a perpendicular vector rotated 90 degrees counter-clockwise.
   *  The dot product between this vector and its perpendicular vector is always zero.
   *
   *  @return a perpendicular [[Vector2D]]
   */
  def perpendicular: Vector2D =
    Vector2D(-this.y, this.x)

  /** Computes the module (length) of this vector.
   *
   *  @return the length of the vector as a [[Double]]
   */
  def module: Double = Math.hypot(x, y)

  /** Computes the squared module of this vector.
   *  Useful to avoid square root calculations when comparing lengths.
   *
   *  @return the squared length of the vector as a [[Double]]
   */
  def moduleSquared: Double = x * x + y * y

  /** Computes a normalized (unit) vector pointing in the same direction as this vector.
   *
   *  If the vector magnitude is `0.0`, returns [[Vector2D.zero]] to avoid division by zero.
   *
   *  @return a unit [[Vector2D]] with a magnitude of `1.0`, or [[Vector2D.zero]] if magnitude is `0.0`
   */
  def normalized: Vector2D =
    val mod = module
    if mod == 0.0 then Vector2D.zero else Vector2D(x / mod, y / mod)

  /** Rotates this vector by a specified angle in degrees.
   *  The rotation is counter-clockwise in the Cartesian plane.
   *
   *  @param angleDegrees the angle in degrees to rotate the vector
   *  @return a new [[Vector2D]] representing the rotated vector
   */
  def rotated(angleDegrees: Double): Vector2D =
    val angleRadians = Math.toRadians(angleDegrees)
    val cosTheta = Math.cos(angleRadians)
    val sinTheta = Math.sin(angleRadians)
    Vector2D(
      x * cosTheta - y * sinTheta, // x' = r * cos(alpha + theta) --> expand
      x * sinTheta + y * cosTheta // y' = r * sin(alpha + theta) --> expand
    )

  /** Computes the angle of this vector in degrees relative to the positive X-axis.
   *  The angle is measured counter-clockwise from the positive X-axis to the vector.
   *
   *  @return the angle in degrees as a [[Double]]
   */
  def angle: Double = math.atan2(y, x).toDegrees

  /** Converts this vector to a [[Point2D]]. */
  def toPoint: Point2D = Point2D(x, y)

object Vector2D:
  /** A constant vector representing the origin `(0.0, 0.0)`. */
  val zero: Vector2D = Vector2D(0.0, 0.0)

  /** A unit vector pointing along the positive X-axis `(1.0, 0.0)`. */
  val unitX: Vector2D = Vector2D(1.0, 0.0)

  /** A unit vector pointing along the positive Y-axis `(0.0, 1.0)`. */
  val unitY: Vector2D = Vector2D(0.0, 1.0)

  /** Creates a vector from a tuple of coordinates. */
  def apply(t: (Double, Double)): Vector2D = Vector2D(t._1, t._2)

  given Conversion[(Double, Double), Vector2D] with
    def apply(t: (Double, Double)): Vector2D = Vector2D(t._1, t._2)

export Vector2D.given

extension (scalar: Double)
  /** Multiplies a scalar by a vector from the left.
   *
   *  @param v the vector to scale
   *  @return a new [[Vector2D]] scaled by this factor
   */
  def *(v: Vector2D): Vector2D = v * scalar
