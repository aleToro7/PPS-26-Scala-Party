package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{Circle, Rectangle, Triangle}

import scala.annotation.tailrec
import scala.reflect.ClassTag

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
enum Shape:
  case Circle(radius: Double, center: Point2D)
  case Rectangle(width: Double, height: Double, center: Point2D)
  case Triangle(a: Point2D, b: Point2D, c: Point2D)

extension [A <: Shape](self: A)

  /** Determines whether the current shape intersects with another shape.
   *
   *  @param other the other shape to check for intersection
   *  @tparam B the type of the other shape, which must be a subtype of Shape
   *  @return true if the shapes intersect, false otherwise
   */
  infix def intersects[B <: Shape](other: B): Boolean = (self, other) match
    case (r1: Rectangle, r2: Rectangle) =>
      val (rect1Left, rect1Right, rect1Bottom, rect1Top) = r1.corners
      val (rect2Left, rect2Right, rect2Bottom, rect2Top) = r2.corners
      val overlapX = math.max(rect1Left, rect2Left) <= math.min(rect1Right, rect2Right)
      val overlapY = math.max(rect1Bottom, rect2Bottom) <= math.min(rect1Top, rect2Top)
      overlapX && overlapY
    case (c1: Circle, c2: Circle) =>
      val distance = (c1.center - c2.center).module
      distance <= c1.radius + c2.radius
    case (t1: Triangle, t2: Triangle) =>
      val edgesT1 = t1.edges
      val edgesT2 = t2.edges
      // 1. Check if any edge of t1 intersects any edge of t2
      val edgesIntersect = edgesT1 anyIntersects edgesT2
      // 2. Check if one triangle contains the other
      edgesIntersect || (t1.a isInside t2) || (t2.a isInside t1)
    case (t: Triangle, r: Rectangle) => t intersects r
    case (r: Rectangle, t: Triangle) => t intersects r
    case _ => false
    

// (b-a) * (c-a) = (b.x - a.x)(c.y - a.y) - (b.y - a.y)(c.x - a.x)
// A positive cross product indicates that point c is to the left of the line formed by points a and b,
// a negative cross product indicates that point c is to the right of the line,
// and a zero cross product indicates that point c is on the line.
private def crossProduct(a: Point2D, b: Point2D, c: Point2D): Double =
  (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

type Segment = (Point2D, Point2D)

extension (self: Segment)

  def intersects(other: Segment): Boolean =
    val (a1, b1) = self
    val (a2, b2) = other
    val ca1 = crossProduct(a1, b1, a2)
    val ca2 = crossProduct(a1, b1, b2)
    val cp3 = crossProduct(a2, b2, a1)
    val cp4 = crossProduct(a2, b2, b1)
    ((ca1 > 0) != (ca2 > 0)) && ((cp3 > 0) != (cp4 > 0))

extension (self: List[Segment])

  def anyIntersects(segments: List[Segment]): Boolean = self.exists:
    segment1 => segments.exists:
      segment2 => segment1 intersects segment2

extension (self: Triangle)

  def edges: List[Segment] = List((self.a, self.b), (self.a, self.c), (self.b, self.c))
  
  def intersects(r: Rectangle): Boolean =
    val rectangleEdges = r.edges
    val triangleEdges = self.edges
    // 1. Check edge intersections between rectangle and triangle
    val edgesIntersect = rectangleEdges anyIntersects triangleEdges
    val rectanglePoint = rectangleEdges.head._1 // take any point of the rectangle
    // 2. Check if the triangle is fully inside the rectangle or vice versa
    edgesIntersect || (self.a isInside r) || (rectanglePoint isInside self)

extension (self: Rectangle)

  def corners: (Double, Double, Double, Double) =
    val left = self.center.x - self.width / 2.0
    val right = self.center.x + self.width / 2.0
    val bottom = self.center.y - self.height / 2.0
    val top = self.center.y + self.height / 2.0
    (left, right, bottom, top)

  def edges: List[Segment] =
    val (left, right, bottom, top) = self.corners
    val a = Point2D(left, bottom)
    val b = Point2D(right, bottom)
    val c = Point2D(right, top)
    val d = Point2D(left, top)
    List((a, b), (a, d), (b, c), (c, d))

extension (self: Point2D)

  def isInside(t: Triangle): Boolean =
    val cp1 = crossProduct(t.a, t.b, self)
    val cp2 = crossProduct(t.b, t.c, self)
    val cp3 = crossProduct(t.c, t.a, self)
    val hasNeg = (cp1 < 0) || (cp2 < 0) || (cp3 < 0)
    val hasPos = (cp1 > 0) || (cp2 > 0) || (cp3 > 0)
    !(hasNeg && hasPos)

  def isInside(r: Rectangle): Boolean =
    val (left, right, bottom, top) = r.corners
    self.x >= left && self.x <= right && self.y >= bottom && self.y <= top

