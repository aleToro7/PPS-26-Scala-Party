package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.utils.half

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
sealed trait Shape extends Projectable:

  /** Returns the center point of the shape. */
  def center: Point2D

  /** Returns the minimal axis-aligned bounding box enclosing the shape. */
  def boundingBox: Shape.AABB

  /** Moves the shape by a given displacement vector. */
  def move(delta: Vector2D): Shape

  /** Moves the shape so that its center aligns with the specified target point. */
  def moveTo(target: Point2D): Shape

  /** Rotates the shape counter-clockwise around its center by a given angle in degrees. */
  def rotate(angleDegrees: Double): Shape

object Shape:
  private type Segment = (Point2D, Point2D)

  /** A convex polygon defined by an ordered sequence of vertices. */
  final case class Polygon(vertices: Point2D*) extends Shape:

    override def center: Point2D =
      val (sumX, sumY) = vertices.foldLeft((0.0, 0.0)):
        case ((accX, accY), v) => (accX + v.x, accY + v.y)
      Point2D(sumX / vertices.size, sumY / vertices.size)

    override def boundingBox: AABB =
      val a = vertices.head
      val (minX, minY, maxX, maxY) = vertices.tail.foldLeft((a.x, a.y, a.x, a.y)):
        case ((currMinX, currMinY, currMaxX, currMaxY), p) =>
          (math.min(currMinX, p.x), math.min(currMinY, p.y), math.max(currMaxX, p.x), math.max(currMaxY, p.y))
      val width = maxX - minX
      val height = maxY - minY
      val center = Point2D(minX + width.half, minY + height.half)
      AABB(width, height, center)

    override def move(delta: Vector2D): Polygon =
      Polygon(vertices.map(_ + delta)*)

    override def moveTo(target: Point2D): Polygon =
      val delta = target - center
      Polygon(vertices.map(_ + delta)*)

    override def rotate(angleDegrees: Double): Polygon =
      val c = center
      Polygon(vertices.map(v => c + (v - c).rotated(angleDegrees))*)

    override def projectOnto(axis: Vector2D): Projection =
      val projections = vertices.map(v => v.toVector.dot(axis))
      Projection(projections.min, projections.max)

    /** Returns the directed edges connecting adjacent vertices of the polygon. */
    def edges: Seq[Segment] =
      vertices.zip(vertices.tail :+ vertices.head)

    /** Computes the perpendicular normal axes of each edge for SAT collision testing. */
    def axes: Seq[Vector2D] =
      edges.map: (v1, v2) =>
        (v2 - v1).perpendicular.normalized

    /** Finds the vertex of this polygon closest to the specified point. */
    def closestVertexTo(point: Point2D): Point2D =
      vertices.minBy(_.distanceSquaredTo(point))

  /** A circle defined by its radius and center point. */
  final case class Circle(radius: Double, override val center: Point2D) extends Shape:

    override def boundingBox: AABB =
      AABB(radius * 2, radius * 2, center)

    override def move(delta: Vector2D): Circle =
      Circle(radius, center + delta)

    override def moveTo(target: Point2D): Circle =
      Circle(radius, target)

    override def rotate(angleDegrees: Double): Circle =
      this

    override def projectOnto(axis: Vector2D): Projection =
      val centerProj = center.toVector.dot(axis)
      Projection(centerProj - radius, centerProj + radius)

  /** An axis-aligned bounding box defined by width, height, and center point. */
  final case class AABB(width: Double, height: Double, override val center: Point2D) extends Shape:

    override def boundingBox: AABB =
      this

    override def move(delta: Vector2D): AABB =
      AABB(width, height, center + delta)

    override def moveTo(target: Point2D): AABB =
      AABB(width, height, target)

    override def rotate(angleDegrees: Double): Shape =
      if angleDegrees % 360.0 == 0.0 then this
      else Polygon(vertices*).rotate(angleDegrees)

    override def projectOnto(axis: Vector2D): Projection =
      val projections = vertices.map(v => v.toVector.dot(axis))
      Projection(projections.min, projections.max)

    /** Returns the vertices of the AABB starting from bottom-left. */
    def vertices: Seq[Point2D] =
      val halfWidth = width.half
      val halfHeight = height.half
      val bottomLeft = Point2D(center.x - halfWidth, center.y - halfHeight)
      Seq(
        bottomLeft,
        Point2D(bottomLeft.x + width, bottomLeft.y),
        Point2D(bottomLeft.x + width, bottomLeft.y + height),
        Point2D(bottomLeft.x, bottomLeft.y + height)
      )

    /** Returns the edges of the AABB. */
    def edges: Seq[Segment] =
      val verts = vertices
      verts.zip(verts.tail :+ verts.head)
