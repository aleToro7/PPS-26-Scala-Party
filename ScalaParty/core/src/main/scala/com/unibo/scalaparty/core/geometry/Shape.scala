package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{Circle, Rectangle, Triangle}

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
enum Shape:
  case Circle(radius: Double, center: Point2D)
  case Rectangle(width: Double, height: Double, center: Point2D)
  case Triangle(a: Point2D, b: Point2D, c: Point2D)

extension [A <: Shape](self: A)

  /** Determines whether the current shape intersects with another shape.
   *  @param other the other shape to check for intersection
   *  @tparam B the type of the other shape, which must be a subtype of Shape
   *  @return true if the shapes intersect, false otherwise
   */
  infix def intersects[B <: Shape](other: B): Boolean = (self, other) match
    case (r1: Rectangle, r2: Rectangle) =>
      val (rect1Left, rect1Right, rect1Bottom, rect1Top) = getCorners(r1)
      val (rect2Left, rect2Right, rect2Bottom, rect2Top) = getCorners(r2)
      val overlapX = math.max(rect1Left, rect2Left) <= math.min(rect1Right, rect2Right)
      val overlapY = math.max(rect1Bottom, rect2Bottom) <= math.min(rect1Top, rect2Top)
      overlapX && overlapY
    case (c1: Circle, c2: Circle) =>
      val distance = (c1.center - c2.center).module
      distance <= c1.radius + c2.radius
    case (t1: Triangle, t2: Triangle) =>
      val edgesT1 = List((t1.a, t1.b), (t1.b, t1.c), (t1.c, t1.a))
      val edgesT2 = List((t2.a, t2.b), (t2.b, t2.c), (t2.c, t2.a))
      // 1. Check if any edge of t1 intersects any edge of t2
      val edgesIntersect = edgesT1.exists:
        case (a1, b1) => edgesT2.exists { case (a2, b2) => segmentsIntersect(a1, b1, a2, b2) }
      // 2. Check if one triangle contains the other
      edgesIntersect || pointInTriangle(t1.a, t2) || pointInTriangle(t2.a, t1)
    case _ => false

// (b-a) * (c-a) = (b.x - a.x)(c.y - a.y) - (b.y - a.y)(c.x - a.x)
// A positive cross product indicates that point c is to the left of the line formed by points a and b,
// a negative cross product indicates that point c is to the right of the line,
// and a zero cross product indicates that point c is on the line.
private def crossProduct(a: Point2D, b: Point2D, c: Point2D): Double =
  (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)

private def pointInTriangle(p: Point2D, t: Triangle): Boolean =
  val cp1 = crossProduct(t.a, t.b, p)
  val cp2 = crossProduct(t.b, t.c, p)
  val cp3 = crossProduct(t.c, t.a, p)
  val hasNeg = (cp1 < 0) || (cp2 < 0) || (cp3 < 0)
  val hasPos = (cp1 > 0) || (cp2 > 0) || (cp3 > 0)
  !(hasNeg && hasPos)

private def segmentsIntersect(a1: Point2D, b1: Point2D, a2: Point2D, b2: Point2D): Boolean =
  val ca1 = crossProduct(a1, b1, a2)
  val ca2 = crossProduct(a1, b1, b2)
  val cp3 = crossProduct(a2, b2, a1)
  val cp4 = crossProduct(a2, b2, b1)
  ((ca1 > 0) != (ca2 > 0)) && ((cp3 > 0) != (cp4 > 0))

private def getCorners(r: Rectangle): (Double, Double, Double, Double) =
  val left = r.center.x - r.width / 2.0
  val right = r.center.x + r.width / 2.0
  val bottom = r.center.y - r.height / 2.0
  val top = r.center.y + r.height / 2.0
  (left, right, bottom, top)
