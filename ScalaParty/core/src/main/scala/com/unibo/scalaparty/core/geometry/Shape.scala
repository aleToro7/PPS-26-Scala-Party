package com.unibo.scalaparty.core.geometry

import java.lang.Math.clamp

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
enum Shape:
  case Polygon(vertices: Point2D*)
  case Circle(radius: Double, center: Point2D)
  case AABB(width: Double, height: Double, center: Point2D)

extension [S <: Shape](self: S)

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
    case (p: Polygon, c: Circle) => p intersects c
    case (c: Circle, p: Polygon) => p intersects c

  /** Moves the shape by a given delta vector.
   *  @param delta the vector by which to move the shape
   *  @return a new shape that is the result of moving the current shape by the specified delta
   */
  def move(delta: Vector2D): S = self match
    case Polygon(vertices*) => Polygon(vertices.map(_ + delta)*).asInstanceOf[S]
    case Circle(radius, center) => Circle(radius, center + delta).asInstanceOf[S]
    case AABB(width, height, center) => AABB(width, height, center + delta).asInstanceOf[S]

  /** Moves the shape to a specific point in space.
   *  @param p the point to which the shape should be moved
   *  @return a new shape that is the result of moving the current shape to the specified point
   */
  def moveTo(p: Point2D): S = self match
    case shape: Polygon => self.move(p - shape.center)
    case Circle(radius, center) => Circle(radius, p).asInstanceOf[S]
    case AABB(width, height, center) => AABB(width, height, p).asInstanceOf[S]

  /** Returns the bounding box of the shape.
   *  @return the bounding box of the shape
   */
  def boundingBox: AABB = self match
    case shape: Polygon => shape.boundingBox
    case Circle(radius, center) => AABB(radius * 2, radius * 2, center)
    case aabb: AABB => aabb

  /** Calculates the minimum translation vector needed to separate two intersecting shapes.
   *  @param other the other shape
   *  @return Some(MTV) if the shapes intersect, None otherwise
   */
  def penetratingVector(other: Shape): Option[Vector2D] = (self, other) match
    case (p1: Polygon, p2: Polygon) => p1.penetratingVector(p2)
    case (p: Polygon, c: Circle) => p.penetratingVector(c)
    case (c: Circle, p: Polygon) => p.penetratingVector(c).map(_ * -1)
    case (r1: AABB, r2: AABB) => r1.penetratingVector(r2)
    case (c1: Circle, c2: Circle) => c1.penetratingVector(c2)
    case (r: AABB, c: Circle) => c.penetratingVector(r).map(_ * -1)
    case (c: Circle, r: AABB) => c.penetratingVector(r)
    case (p: Polygon, r: AABB) => p.penetratingVector(r)
    case (r: AABB, p: Polygon) => p.penetratingVector(r).map(_ * -1)

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

extension (self: Double)
  private def half = self / 2.0

extension (self: Polygon)
  private def edges: Seq[Segment] = self.vertices.zip(self.vertices.tail :+ self.vertices.head)

  private def axes: Seq[Vector2D] = self.edges.map: (v1, v2) =>
    val v = (v2 - v1).normalized
    Vector2D(-v.x, v.y)

  private def closestVertexTo(point: Point2D): Point2D =
    self.vertices.minBy: v =>
      val d = point - v
      (d.x * d.x) + (d.y * d.y)

  /** Checks if the polygon intersects with another polygon using the Separating Axis Theorem (SAT).
   *  @param other the other polygon to check for intersection
   *  @return true if they intersect, false otherwise
   */
  def intersects(other: Polygon): Boolean =
    val axes = self.axes ++ other.axes
    !self.hasSeparatingAxis(axes)(other)

  /** Checks if the polygon intersects with an AABB.
   *  @param aabb the AABB to check for intersection
   *  @return true if they intersect, false otherwise
   */
  def intersects(aabb: AABB): Boolean = self intersects Polygon(aabb.vertices*)

  /** Checks if the polygon intersects with a circle.
   *  @param circle the circle to check for intersection
   *  @return true if they intersect, false otherwise
   */
  def intersects(circle: Circle): Boolean =
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

  /** Calculates the minimum translation vector needed to separate two intersecting polygons.
   *
   *  @param other the other polygon
   *  @return [[Some]] containing the penetration vector if colliding, or [[None]] otherwise.
   */
  def penetratingVector(other: Polygon): Option[Vector2D] =
    computeMtv(self.axes ++ other.axes, other, other.center)

  /** Calculates the minimum translation vector needed to separate a polygon and a circle.
   *
   *  @param circle the target circle to check against
   *  @return [[Some]] containing the penetration vector pointing towards the circle if colliding, or [[None]] otherwise.
   */
  def penetratingVector(circle: Circle): Option[Vector2D] =
    val closestVertex = self.closestVertexTo(circle.center)
    val vertexAxis = circle.center - closestVertex
    val candidateAxes =
      if vertexAxis.x == 0.0 && vertexAxis.y == 0.0 then self.axes
      else self.axes :+ vertexAxis.normalized
    computeMtv(candidateAxes, circle, circle.center)

  private def computeMtv(
      candidateAxes: Seq[Vector2D],
      target: Projectable,
      targetCenter: Point2D
  ): Option[Vector2D] =
    self.minOverlappingAxis(candidateAxes)(target).map: (axis, overlap) =>
      val direction = targetCenter - self.center
      val dotProduct = axis.x * direction.x + axis.y * direction.y
      val alignedAxis = if dotProduct < 0 then axis * -1 else axis
      alignedAxis * overlap

  /** Calculates the minimum translation vector needed to separate a polygon and an AABB.
   *  @param aabb the AABB
   *  @return Some(MTV) if the polygon and AABB intersect, None otherwise
   */
  def penetratingVector(aabb: AABB): Option[Vector2D] = self.penetratingVector(Polygon(aabb.vertices*))

  /** Computes and returns the polygon's bounding box.
   *  The computed bounded box is the smallest [[AABB]] object fitting the polygon.
   *  @return the computed bounding box
   */
  def boundingBox: AABB =
    val a = self.vertices.head
    val (minX, minY, maxX, maxY) = self.vertices.tail.foldLeft((a.x, a.y, a.x, a.y)):
      case ((currMinX, currMinY, currMaxX, currMaxY), p) =>
        (math.min(currMinX, p.x), math.min(currMinY, p.y), math.max(currMaxX, p.x), math.max(currMaxY, p.y))
    val width = maxX - minX
    val height = maxY - minY
    val center = Point2D(minX + width.half, minY + height.half)
    AABB(width, height, center)

  /** Computes and returns the center point of the polygon.
   *  @return the computed center point of the polygon
   */
  def center: Point2D =
    val vertices = self.vertices
    vertices.foldLeft(Point2D(0.0, 0.0))((acc, v) =>
      Point2D(acc.x + (v.x / vertices.size), acc.y + (v.y / vertices.size))
    )

  /** Rotates the polygon by a given angle (in degrees) around its center.
   *  @param angle the angle in degrees by which to rotate the polygon
   *  @return a new [[Polygon]] that is the result of rotating the current polygon by the specified angle
   */
  def rotate(angle: Double): Polygon =
    val radians = math.toRadians(angle)
    val cosTheta = math.cos(radians)
    val sinTheta = math.sin(radians)
    val center = self.center
    val rotatedVertices = self.vertices.map { vertex =>
      val translatedX = vertex.x - center.x
      val translatedY = vertex.y - center.y
      val rotatedX = translatedX * cosTheta - translatedY * sinTheta
      val rotatedY = translatedX * sinTheta + translatedY * cosTheta
      Point2D(rotatedX + center.x, rotatedY + center.y)
    }
    Polygon(rotatedVertices*)

  def move(v: Vector2D): Polygon =
    val movedVertices = self.vertices.map(_ + v)
    Polygon(movedVertices*)

  /** Moves the polygon to a specific point in space.
   *  @param p the point to which the polygon should be moved
   *  @return a new [[Polygon]] that is the result of moving the current polygon to the specified point
   */
  def moveTo(p: Point2D): Polygon =
    val delta = p - self.center
    val movedVertices = self.vertices.map(_ + delta)
    Polygon(movedVertices*)

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

  /** Checks if this AABB intersects with another AABB.
   *  @param other the other AABB to check for intersection
   *  @return true if the two AABBs intersect, false otherwise
   */
  def intersects(other: AABB): Boolean =
    // This implementation could actually be much prettier, however the objective of AABB is performance
    val dx = math.abs(self.center.x - other.center.x)
    val dy = math.abs(self.center.y - other.center.y)
    dx <= self.width.half + other.width.half && dy <= self.height.half + other.height.half

  /** Calculates the minimum translation vector needed to separate two intersecting AABBs.
   *  @param other the other AABB
   *  @return Some(MTV) if the AABBs intersect, None otherwise
   */
  def penetratingVector(other: AABB): Option[Vector2D] =
    val delta = other.center - self.center
    val overlapX = (self.width.half + other.width.half) - math.abs(delta.x)
    val overlapY = (self.height.half + other.height.half) - math.abs(delta.y)
    Option.when(overlapX > 0 && overlapY > 0):
      // We're looking for the minimum vector, which is either on the x or y axis, depending on which overlap is smaller
      if overlapX < overlapY then
        Vector2D(if delta.x < 0 then -overlapX else overlapX, 0)
      else
        Vector2D(0, if delta.y < 0 then -overlapY else overlapY)

extension (self: Circle)
  /** Checks if the circle intersects with another circle.
   *  @param c the other circle to check for intersection
   *  @return true if they intersect, false otherwise
   */
  def intersects(c: Circle): Boolean =
    val distance = (self.center - c.center).module
    distance <= self.radius + c.radius

  /** Checks if the circle intersects with an AABB.
   *  @param aabb the AABB to check for intersection
   *  @return true if they intersect, false otherwise
   */
  def intersects(aabb: AABB): Boolean = self.penetratingVector(aabb).isDefined

  /** Calculates the minimum translation vector needed to separate two intersecting circles.
   *  @param other the other circle
   *  @return Some(MTV) if the circles intersect, None otherwise
   */
  def penetratingVector(other: Circle): Option[Vector2D] =
    val delta = other.center - self.center
    val distance = delta.module
    val overlap = self.radius + other.radius - distance
    Option.when(overlap > 0):
      delta.normalized * overlap

  /** Calculates the minimum translation vector needed to separate a circle and an AABB.
   *  @param aabb the AABB
   *  @return Some(MTV) if the circle and AABB intersect, None otherwise
   */
  def penetratingVector(aabb: AABB): Option[Vector2D] =
    val distanceBetweenCenters = self.center - aabb.center
    val halfWidth = aabb.width.half
    val halfHeight = aabb.height.half
    val px = clamp(distanceBetweenCenters.x, -halfWidth, halfWidth)
    val py = clamp(distanceBetweenCenters.y, -halfHeight, halfHeight)
    val closestPoint = Point2D(aabb.center.x + px, aabb.center.y + py)
    val distanceFromClosest = self.center - closestPoint
    val squaredDistance = distanceFromClosest.x * distanceFromClosest.x + distanceFromClosest.y * distanceFromClosest.y
    Option.when(squaredDistance < self.radius * self.radius):
      val distance = math.sqrt(squaredDistance)
      val overlap = self.radius - distance
      distanceFromClosest.normalized * overlap
