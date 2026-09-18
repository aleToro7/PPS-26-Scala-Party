package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class IntersectionSpec extends AnyFlatSpec:

  "A Rectangle" should "intersect when they overlap" in:
    val a = AABB(4.0, 2.0, Point2D(0.0, 0.0))
    val b = AABB(2.0, 2.0, Point2D(1.0, 0.0))
    (a intersects b) shouldBe true

  it should "intersect when they touch only at the border" in:
    val a = AABB(4.0, 2.0, Point2D(0.0, 0.0))
    val b = AABB(2.0, 2.0, Point2D(3.0, 0.0))
    (a intersects b) shouldBe true

  it should "intersect when one rectangle contains the other" in:
    val outerRect = AABB(10.0, 10.0, Point2D(0.0, 0.0))
    val innerRect = AABB(2.0, 2.0, Point2D(1.0, 1.0))
    (outerRect intersects innerRect) shouldBe true

  it should "not intersect when they are separated by a gap" in:
    val a = AABB(4.0, 2.0, Point2D(0.0, 0.0))
    val b = AABB(2.0, 2.0, Point2D(3.01, 0.0))
    (a intersects b) shouldBe false

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

  it should "compute the correct bounding box" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    triangle.boundingBox shouldBe AABB(4.0, 4.0, Point2D(2.0, 2.0))

  it should "compute the correct bounding box for a triangle with negative coordinates" in:
    val triangle = Triangle(Point2D(-2.0, -1.0), Point2D(1.0, -3.0), Point2D(-4.0, 2.0))
    triangle.boundingBox shouldBe AABB(5.0, 5.0, Point2D(-1.5, -0.5))
