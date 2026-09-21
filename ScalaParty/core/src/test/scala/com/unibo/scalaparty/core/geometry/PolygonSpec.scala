package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class PolygonSpec extends AnyFlatSpec:

  type Triangle = Polygon

  object Triangle:
    def apply(a: Point2D, b: Point2D, c: Point2D): Triangle = Polygon(a, b, c)

  given Conversion[Triangle, Polygon] with
    def apply(t: Triangle): Polygon = t

  "A Triangle" should "intersect when they overlap" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val b = Triangle(Point2D(1.0, 1.0), Point2D(3.0, 1.0), Point2D(2.0, 3.0))
    (a intersects b) shouldBe true

  it should "intersect when they touch only at the border" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(0.0, 2.0), Point2D(2.0, 0.0))
    val b = Triangle(Point2D(0.0, 0.0), Point2D(0.0, 2.0), Point2D(-2.0, 0.0))
    (a intersects b) shouldBe true

  it should "not intersect when they are separated by a gap" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val b = Triangle(Point2D(3.0, 3.0), Point2D(5.0, 3.0), Point2D(4.0, 5.0))
    (a intersects b) shouldBe false

  it should "intersect when one triangle is fully inside the other" in:
    val outerTriangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val innerTriangle = Triangle(Point2D(1.0, 1.0), Point2D(3.0, 1.0), Point2D(2.0, 3.0))
    (outerTriangle intersects innerTriangle) shouldBe true

  it should "intersect with a rectangle when they overlap" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val rectangle = AABB(3.0, 3.0, Point2D(1.0, 1.0))
    (rectangle intersects triangle) shouldBe true

  it should "intersect with a rectangle when they touch only at the border" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 0.0))
    val rectangle = AABB(2.0, 2.0, Point2D(-1.0, 0.0))
    (rectangle intersects triangle) shouldBe true

  it should "intersect with a circle when they overlap" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(2.0, Point2D(2.0, 1.0))
    (triangle intersects circle) shouldBe true

  it should "intersect with a circle when they touch only at the border" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(2.0, Point2D(2.0, 4.0))
    (triangle intersects circle) shouldBe true

  it should "intersect with a circle when one contains the other" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(1.0, Point2D(2.0, 1.0))
    (triangle intersects circle) shouldBe true

  it should "not intersect with a circle when they are separated by a gap" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(1.0, Point2D(5.0, 5.0))
    (triangle intersects circle) shouldBe false

  "A Triangle" should "compute the correct bounding box" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    triangle.boundingBox shouldBe AABB(4.0, 4.0, Point2D(2.0, 2.0))

  it should "compute the correct bounding box for a triangle with negative coordinates" in:
    val triangle = Triangle(Point2D(-2.0, -1.0), Point2D(1.0, -3.0), Point2D(-4.0, 2.0))
    triangle.boundingBox shouldBe AABB(5.0, 5.0, Point2D(-1.5, -0.5))

  it should "compute the correct bounding box for a triangle with zero coordinates" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(0.0, 0.0), Point2D(0.0, 0.0))
    triangle.boundingBox shouldBe AABB(0.0, 0.0, Point2D(0.0, 0.0))

  "A Triangle" should "compute the correct center point" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    triangle.center shouldBe Point2D(3.0, 2.0)

  it should "compute the correct center point for a triangle with negative coordinates" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(-6.0, 0.0), Point2D(-3.0, -6.0))
    triangle.center shouldBe Point2D(-3.0, -2.0)

  "A Triangle" should "be correctly moved to a new position" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    val newCenter = Point2D(3.0, 3.0)
    val expectedTriangle = Triangle(Point2D(0.0, 1.0), Point2D(6.0, 1.0), Point2D(3.0, 7.0))
    triangle.moveTo(newCenter) shouldEqual expectedTriangle

  it should "be correctly moved to a new negative position" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    val newCenter = Point2D(-3.0, -3.0)
    val expectedTriangle = Triangle(Point2D(-6.0, -5.0), Point2D(0.0, -5.0), Point2D(-3.0, 1.0))
    triangle.moveTo(newCenter) shouldEqual expectedTriangle

  "A Triangle" should "correctly rotate around its center" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    val expectedTriangle = Triangle(Point2D(6.0, 4.0), Point2D(0.0, 4.0), Point2D(3.0, -2.0))
    triangle.rotate(180) shouldEqual expectedTriangle

  it should "correctly return to its original position after a full rotation" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    triangle.rotate(360) shouldEqual triangle

  it should "correctly perform multiple rotations" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    triangle.rotate(90).rotate(90).rotate(90).rotate(90) shouldEqual triangle

  it should "correctly rotate with negative angles" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 6.0))
    val expectedTriangle = Triangle(Point2D(6.0, 4.0), Point2D(0.0, 4.0), Point2D(3.0, -2.0))
    triangle.rotate(-180) shouldEqual expectedTriangle

  extension (self: Triangle)
    def shouldEqual(other: Triangle): Unit =
      val tolerance = 0.0001
      for (v1, v2) <- self.vertices zip other.vertices do
        v1.x shouldBe v2.x +- tolerance
        v1.y shouldBe v2.y +- tolerance
