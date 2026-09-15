package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class IntersectionSpec extends AnyFlatSpec:

  "A Rectangle" should "intersect when they overlap" in:
    val a = Shape.Rectangle(4.0, 2.0, Point2D(0.0, 0.0))
    val b = Shape.Rectangle(2.0, 2.0, Point2D(1.0, 0.0))
    (a intersects b) shouldBe true

  it should "intersect when they touch only at the border" in:
    val a = Shape.Rectangle(4.0, 2.0, Point2D(0.0, 0.0))
    val b = Shape.Rectangle(2.0, 2.0, Point2D(3.0, 0.0))
    (a intersects b) shouldBe true

  it should "intersect when one rectangle contains the other" in:
    val outerRect = Shape.Rectangle(10.0, 10.0, Point2D(0.0, 0.0))
    val innerRect = Shape.Rectangle(2.0, 2.0, Point2D(1.0, 1.0))
    (outerRect intersects innerRect) shouldBe true

  it should "not intersect when they are separated by a gap" in:
    val a = Shape.Rectangle(4.0, 2.0, Point2D(0.0, 0.0))
    val b = Shape.Rectangle(2.0, 2.0, Point2D(3.01, 0.0))
    (a intersects b) shouldBe false

  "A Circle" should "intersect when they overlap" in:
    val a = Shape.Circle(2.0, Point2D(0.0, 0.0))
    val b = Shape.Circle(2.0, Point2D(1.0, 1.0))
    (a intersects b) shouldBe true

  it should "intersect when they touch only at the border" in:
    val a = Shape.Circle(2.0, Point2D(0.0, 0.0))
    val b = Shape.Circle(2.0, Point2D(4.0, 0.0))
    (a intersects b) shouldBe true

  it should "not intersect when they are separated by a gap" in:
    val a = Shape.Circle(2.0, Point2D(0.0, 0.0))
    val b = Shape.Circle(2.0, Point2D(5.0, 0.0))
    (a intersects b) shouldBe false
