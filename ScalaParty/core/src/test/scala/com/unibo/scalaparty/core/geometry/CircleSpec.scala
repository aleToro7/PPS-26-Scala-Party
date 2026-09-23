package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle}
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.defined
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

class CircleSpec extends AnyFlatSpec with Matchers:

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
    result.get.x shouldBe (0.6 +- 1e-9)
    result.get.y shouldBe (0.8 +- 1e-9)
