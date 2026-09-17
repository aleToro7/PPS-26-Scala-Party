package com.unibo.scalaparty.core.geometry

import java.lang.Math.clamp

given Conversion[AABB, Polygon] = r => Polygon(r.vertices*)

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
enum Shape:
  case Polygon(vertices: Point2D*)
  case Circle(radius: Double, center: Point2D)
  case AABB(width: Double, height: Double, center: Point2D)

extension [A <: Shape](self: A)

  /** Determines whether the current shape intersects with another shape.
   *
   *  @param other the other shape to check for intersection
   *  @tparam B the type of the other shape, which must be a subtype of Shape
   *  @return true if the shapes intersect, false otherwise
   */
  def intersects[B <: Shape](other: B): Boolean = (self, other) match
    case (r1: AABB, r2: AABB) => r1 intersects r2
    case (c1: Circle, c2: Circle) => c1 intersects c2
    case (p1: Polygon, p2: Polygon) => p1 intersects p2
    case (r: AABB, p: Polygon) => p intersects r
    case (p: Polygon, r: AABB) => p intersects r
    case (c: Circle, r: AABB) => c intersects r
    case (r: AABB, c: Circle) => c intersects r
    case _ => false // TODO: Implement other shape intersections

import com.unibo.scalaparty.core.geometry.Shape.*

private type Segment = (Point2D, Point2D)

given Conversion[Polygon, Projectable] with
  def apply(p: Polygon): Projectable = axis =>
    val projections = p.vertices.map: v =>
      val projection = v.x * axis.x + v.y * axis.y
      projection
    (projections.min, projections.max)

given Conversion[Circle, Projectable] with
  def apply(c: Circle): Projectable = axis =>
    val centerProjection = c.center.x * axis.x + c.center.y * axis.y
    (centerProjection - c.radius, centerProjection + c.radius)

extension (self: Polygon)
  private def edges: Seq[Segment] = self.vertices.zip(self.vertices.tail :+ self.vertices.head)

  private def axes: Seq[Vector2D] = self.edges.map: (v1, v2) =>
    val v = (v2 - v1).normalized
    Vector2D(-v.x, v.y)

  private def closestVertexTo(point: Point2D): Point2D =
    self.vertices.minBy: v =>
      val d = point - v
      (d.x * d.x) + (d.y * d.y)

  private def intersects(other: Polygon): Boolean =
    val axes = self.axes ++ other.axes
    !self.hasSeparatingAxis(axes)(other)
    
  private def intersects(aabb: AABB): Boolean = self intersects Polygon(aabb.vertices*)

  private def intersects(circle: Circle): Boolean =
    val polyHasSeparatingAxis = self.hasSeparatingAxis(self.axes)(circle)
    if polyHasSeparatingAxis then
      return false
    val closestVertex = self closestVertexTo circle.center
    val axisVector = circle.center - closestVertex
    if axisVector.x == 0.0 && axisVector.y == 0.0 then
      return true
    val circleAxis = axisVector.normalized
    val (pMin, pMax) = self projectOnto circleAxis
    val (cMin, cMax) = circle projectOnto circleAxis
    val circleHasSeparatingAxis = pMax < cMin || cMax < pMin
    !circleHasSeparatingAxis

extension (self: Double)
  private def half = self / 2.0

extension (self: AABB)
  private def vertices: Seq[Point2D] =
    val halfWidth = self.width.half
    val halfHeight = self.height.half
    val bottomLeft = Point2D(self.center.x - halfWidth, self.center.y - halfHeight)
    Seq(
      bottomLeft,
      Point2D(bottomLeft.x + self.width, bottomLeft.y), // Bottom-right
      Point2D(bottomLeft.x + self.width, bottomLeft.y + self.height), // Top-right
      Point2D(bottomLeft.x, bottomLeft.y + self.height) // Top-left
    )

  private def edges: Seq[Segment] = Polygon(self.vertices*).edges

  private def intersects(other: AABB): Boolean =
    // This implementation could actually be much prettier, however the objective of AABB is performance
    val dx = math.abs(self.center.x - other.center.x)
    val dy = math.abs(self.center.y - other.center.y)
    dx <= self.width.half + other.width.half && dy <= self.height.half + other.height.half

extension (self: Circle)
  private def intersects(c: Circle): Boolean =
    val distance = (self.center - c.center).module
    distance <= self.radius + c.radius

  private def intersects(aabb: AABB): Boolean =
    val distanceBetweenCenters = self.center - aabb.center
    val halfWidth = aabb.width.half
    val halfHeight = aabb.height.half
    val px = clamp(distanceBetweenCenters.x, -halfWidth, halfWidth)
    val py = clamp(distanceBetweenCenters.y, -halfHeight, halfHeight)
    val closestPoint = Point2D(aabb.center.x + px, aabb.center.y + py)
    val distanceFromClosest = self.center - closestPoint
    val squaredDistance = distanceFromClosest.x * distanceFromClosest.x + distanceFromClosest.y * distanceFromClosest.y
    squaredDistance <= self.radius * self.radius
