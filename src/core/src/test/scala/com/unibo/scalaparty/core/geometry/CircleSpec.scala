package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle}
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.defined
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

class CircleSpec extends AnyFlatSpec with Matchers:
  val tolerance = 1e-6

  "A Circle" should "intersect when they overlap" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(1.0, 1.0))
    (a intersects b) shouldBe true

  it should "intersect when they touch only at the border" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(4.0, 0.0))
    (a intersects b) shouldBe true

  it should "not intersect when they are separated by a gap" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(5.0, 0.0))
    (a intersects b) shouldBe false

  it should "intersect with a rectangle when they overlap" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(4.0, 2.0, Point2D(1.0, 0.0))
    (circle intersects rectangle) shouldBe true

  it should "intersect with a rectangle when they touch only at the border" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(4.0, 2.0, Point2D(2.0, 0.0))
    (circle intersects rectangle) shouldBe true

  it should "intersect with a rectangle when one contains the other" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(1.0, 1.0, Point2D(0.0, 0.0))
    (circle intersects rectangle) shouldBe true

  "penetratingVector" should "return None when circles are not intersecting" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(5.0, 0.0))
    a.penetratingVector(b) shouldBe None

  it should "return None when circles touch only at the border" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(4.0, 0.0))
    a.penetratingVector(b) shouldBe None

  it should "calculate the correct Minimum Translation Vector when overlapping along the X axis" in:
    val a = Circle(2.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(3.0, 0.0))
    a.penetratingVector(b) shouldBe Some(Vector2D(1.0, 0.0))
    b.penetratingVector(a) shouldBe Some(Vector2D(-1.0, 0.0))

  it should "calculate the correct MTV when overlapping along a diagonal direction" in:
    val a = Circle(3.0, Point2D(0.0, 0.0))
    val b = Circle(3.0, Point2D(3.0, 4.0))
    // Distance = 5.0
    // Direction vector = (3/5, 4/5) = (0.6, 0.8)
    // Expected MTV = (0.6, 0.8)
    val result = a penetratingVector b
    result shouldBe defined
    result.get.x shouldBe (0.6 +- tolerance)
    result.get.y shouldBe (0.8 +- tolerance)

  "Circle and AABB penetratingVector" should "return None when Circle and AABB do not intersect" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(4.0, 2.0, Point2D(5.0, 0.0))
    circle.penetratingVector(rectangle) shouldBe None

  it should "return None when Circle and AABB touch only at the border" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(4.0, 2.0, Point2D(4.0, 0.0))
    circle.penetratingVector(rectangle) shouldBe None

  it should "calculate the correct MTV when overlapping along a face" in:
    val circle = Circle(2.0, Point2D(0.0, 0.0))
    val rectangle = AABB(4.0, 4.0, Point2D(3.0, 0.0))
    // Circle radius = 2.0, center = (0,0)
    // AABB left edge = 3.0 - 2.0 = 1.0
    // Overlap along X = 2.0 - 1.0 = 1.0
    circle.penetratingVector(rectangle) shouldBe Some(Vector2D(-1.0, 0.0))
    rectangle.penetratingVector(circle) shouldBe Some(Vector2D(1.0, 0.0))

  it should "calculate the correct MTV when overlapping at a corner" in:
    // Circle centered at origin with radius 2.5 to ensure overlap with corner (2,2)
    val radius = 4.0
    val circle = Circle(radius, Point2D(0.0, 0.0))
    val side = 2.0
    // closest corner to origin is (2, 2)
    val square = AABB(side, side, Point2D(3.0, 3.0))
    val result = circle.penetratingVector(square)
    result shouldBe defined
    val cornerDist = math.hypot(side, side)
    val overlapDepth = 4.0 - cornerDist
    val expectedX = -(side / cornerDist) * overlapDepth
    val expectedY = -(side / cornerDist) * overlapDepth
    result.get.x shouldBe (expectedX +- tolerance)
    result.get.y shouldBe (expectedY +- tolerance)

  it should "calculate a non-zero MTV for concentric overlapping circles" in:
    val a = Circle(3.0, Point2D(0.0, 0.0))
    val b = Circle(2.0, Point2D(0.0, 0.0))
    val mtv = a.penetratingVector(b)
    mtv shouldBe defined
    mtv.get.module shouldBe (5.0 +- tolerance)

  it should "calculate a non-zero MTV when the circle center is completely inside an AABB" in:
    val circle = Circle(1.0, Point2D(0.5, 0.0))
    val square = AABB(4.0, 4.0, Point2D(0.0, 0.0))
    // Square extends from -2 to +2 on X and Y.
    // Circle center is at (0.5, 0.0). Closest face is right face at X = 2.0 (distance 1.5 vs left distance 2.5).
    // Circle should be pushed towards the right with overlap = 1.5 + 1.0 = 2.5
    val mtv = circle.penetratingVector(square)
    mtv shouldBe defined
    mtv.get shouldBe Vector2D(-2.5, 0.0)
