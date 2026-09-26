package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import org.scalactic.Tolerance.convertNumericToPlusOrMinusWrapper
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{be, defined}
import org.scalatest.matchers.should.Matchers.{should, shouldBe}

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

  "A Triangle penetratingVector with AABB" should "return None when they do not intersect" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val rectangle = AABB(2.0, 2.0, Point2D(5.0, 0.0))
    triangle.penetratingVector(rectangle) shouldBe None

  it should "return None when they touch only at the border" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val rectangle = AABB(2.0, 2.0, Point2D(3.0, 0.0))
    triangle.penetratingVector(rectangle) shouldBe None

  it should "calculate the correct MTV along X axis when X overlap is minimal" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 6.0))
    val rectangle = AABB(4.0, 10.0, Point2D(-1.0, 2.0))
    // Target AABB is to the left of Triangle center -> MTV = (-1.0, 0.0)
    triangle.penetratingVector(rectangle) shouldBe Some(Vector2D(-1.0, 0.0))

  it should "calculate the correct MTV along Y axis when Y overlap is minimal" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(6.0, 0.0), Point2D(3.0, 4.0))
    val rectangle = AABB(10.0, 4.0, Point2D(3.0, -1.0))
    // Target AABB is below Triangle center -> MTV = (0.0, -1.0)
    triangle.penetratingVector(rectangle) shouldBe Some(Vector2D(0.0, -1.0))

  "A Polygon penetratingVector with another Polygon" should "return None when polygons do not intersect" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val b = Triangle(Point2D(5.0, 0.0), Point2D(7.0, 0.0), Point2D(6.0, 2.0))
    a.penetratingVector(b) shouldBe None

  it should "return None when polygons touch only at the border" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(2.0, 0.0), Point2D(1.0, 2.0))
    val b = Triangle(Point2D(2.0, 0.0), Point2D(4.0, 0.0), Point2D(3.0, 2.0))
    a.penetratingVector(b) shouldBe None

  it should "return a Minimum Translation Vector when two triangles overlap" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(0.0, 4.0))
    val b = Triangle(Point2D(1.0, 0.0), Point2D(5.0, 0.0), Point2D(1.0, 4.0))
    val resultAtoB = a.penetratingVector(b)
    val resultBtoA = b.penetratingVector(a)
    resultBtoA shouldBe defined
    resultAtoB shouldBe defined

  it should "return opposite Minimum Translation Vectors for overlapping triangles" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(0.0, 4.0))
    val b = Triangle(Point2D(1.0, 0.0), Point2D(5.0, 0.0), Point2D(1.0, 4.0))
    val mtvA = a.penetratingVector(b).get
    val mtvB = b.penetratingVector(a).get
    mtvA.x shouldBe (-mtvB.x +- 1e-4)
    mtvA.y shouldBe (-mtvB.y +- 1e-4)

  it should "push the second triangle away from the first one" in:
    val a = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(0.0, 4.0))
    val b = Triangle(Point2D(1.0, 0.0), Point2D(5.0, 0.0), Point2D(1.0, 4.0))
    val mtvA = a.penetratingVector(b).get
    // The MTV should push b away from a
    val direction = b.center - a.center
    (mtvA.x * direction.x + mtvA.y * direction.y) should be > 0.0

  "A Polygon penetratingVector with Circle" should "return None when they do not intersect" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(1.0, Point2D(8.0, 8.0))
    triangle.penetratingVector(circle) shouldBe None

  it should "return None when they touch only at the border" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(1.0, Point2D(-1.0, 0.0))
    triangle.penetratingVector(circle) shouldBe None

  it should "calculate the correct MTV when overlapping along a flat edge" in:
    val triangle = Triangle(Point2D(0.0, 0.0), Point2D(4.0, 0.0), Point2D(2.0, 4.0))
    val circle = Circle(2.0, Point2D(-1.0, 0.0))
    // Vector pointing towards circle center = (0.0, -1.0)
    val result = triangle.penetratingVector(circle)
    result shouldBe defined
    result.get.y shouldBe (0.0 +- 1e-4)
    result.get.x shouldBe (-1.0 +- 1e-4)

  extension (self: Triangle)
    def shouldEqual(other: Triangle): Unit =
      val tolerance = 0.0001
      for (v1, v2) <- self.vertices zip other.vertices do
        v1.x shouldBe v2.x +- tolerance
        v1.y shouldBe v2.y +- tolerance
