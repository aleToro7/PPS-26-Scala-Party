package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class Point2DSpec extends AnyFlatSpec:

  "A Point2D" should "provide an origin point constant at (0.0, 0.0)" in :
    Point2D.origin shouldBe Point2D(0.0, 0.0)

  "A Point2D" should "correctly add a vector to a point" in :
    val p = Point2D(1.0, 2.0)
    val v = Vector2D(3.0, 4.0)
    (p + v) shouldBe Point2D(4.0, 6.0)

  "A Point2D" should "correctly compute the vector between two points" in :
    val p1 = Point2D(1.0, 7.0)
    val p2 = Point2D(2.0, 3.0)
    (p1 - p2) shouldBe Vector2D(-1.0, 4.0)

  "A Point2D" should "have distance of zero when subtracted from itself" in :
    val p = Point2D(5.0, 5.0)
    (p - p) shouldBe Vector2D.zero

