package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{Circle, Rectangle}

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
    case _ => false

private def getCorners(r: Rectangle): (Double, Double, Double, Double) =
  val left = r.center.x - r.width / 2.0
  val right = r.center.x + r.width / 2.0
  val bottom = r.center.y - r.height / 2.0
  val top = r.center.y + r.height / 2.0
  (left, right, bottom, top)
