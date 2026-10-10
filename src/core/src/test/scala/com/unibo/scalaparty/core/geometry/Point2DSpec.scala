package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class Point2DSpec extends AnyFlatSpec:

  "A Point2D" should "provide an origin point constant at (0.0, 0.0)" in:
    Point2D.origin shouldBe Point2D(0.0, 0.0)

  it should "correctly add a vector to a point" in:
    val p = Point2D(1.0, 2.0)
    val v = Vector2D(3.0, 4.0)
    (p + v) shouldBe Point2D(4.0, 6.0)

  it should "correctly compute the vector between two points" in:
    val p1 = Point2D(1.0, 7.0)
    val p2 = Point2D(2.0, 3.0)
    (p1 - p2) shouldBe Vector2D(-1.0, 4.0)

  it should "have distance of zero when subtracted from itself" in:
    val p = Point2D(5.0, 5.0)
    (p - p) shouldBe Vector2D.zero

  it should "compute the correct distance to another point" in:
    val p1 = Point2D(1.0, 2.0)
    val p2 = Point2D(4.0, 6.0)
    p1.distanceTo(p2) shouldBe 5.0

  it should "compute the correct squared Euclidean distance to another point" in:
    val p1 = Point2D(1.0, 2.0)
    val p2 = Point2D(4.0, 6.0)
    p1.distanceSquaredTo(p2) shouldBe 25.0

  it should "convert correctly to a Vector2D" in:
    val p = Point2D(3.5, -2.5)
    p.toVector shouldBe Vector2D(3.5, -2.5)

  it should "implicitly convert from a (Double, Double) tuple" in:
    val tuple: (Double, Double) = (10.0, 20.0)
    val p: Point2D = tuple
    p shouldBe Point2D(10.0, 20.0)
