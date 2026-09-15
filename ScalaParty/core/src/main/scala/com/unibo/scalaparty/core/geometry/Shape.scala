package com.unibo.scalaparty.core.geometry

/** Represents a geometric shape in a two-dimensional space.
 *  This sealed trait defines the different types of shapes that can be represented, including circles, rectangles, squares, and polygons.
 */
enum Shape:
  case Circle(radius: Double, center: Point2D)
  case Rectangle(width: Double, height: Double, center: Point2D)
  case Square(side: Double, center: Point2D)
  case Triangle(a: Point2D, b: Point2D, c: Point2D)
  
private val tolerance = 1e-6

extension [A <: Shape](self: A)

  /** Determines whether the current shape intersects with another shape.
   *  @param other the other shape to check for intersection
   *  @tparam B the type of the other shape, which must be a subtype of Shape
   *  @return true if the shapes intersect, false otherwise
   */
  infix def intersects[B <: Shape](other: B): Boolean = (self, other) match
    case (Shape.Rectangle(width1, height1, center1), Shape.Rectangle(width2, height2, center2)) =>
      val (rect1Left, rect1Right, rect1Bottom, rect1Top) = getCorners(Shape.Rectangle(width1, height1, center1))
      val (rect2Left, rect2Right, rect2Bottom, rect2Top) = getCorners(Shape.Rectangle(width2, height2, center2))

      val overlapX = math.max(rect1Left, rect2Left) <= math.min(rect1Right, rect2Right)
      val overlapY = math.max(rect1Bottom, rect2Bottom) <= math.min(rect1Top, rect2Top)
      overlapX && overlapY
    case _ => false
    

/** Returns the four corners of a rectangle.
 *  @param r the rectangle
 *  @return a tuple containing the left, right, bottom, and top coordinates
 */
private def getCorners(r: Shape.Rectangle): (Double, Double, Double, Double) =
  val left = r.center.x - r.width / 2.0
  val right = r.center.x + r.width / 2.0
  val bottom = r.center.y - r.height / 2.0
  val top = r.center.y + r.height / 2.0
  (left, right, bottom, top)