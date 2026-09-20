package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.shouldBe

class CircleSpec extends AnyFlatSpec:

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
