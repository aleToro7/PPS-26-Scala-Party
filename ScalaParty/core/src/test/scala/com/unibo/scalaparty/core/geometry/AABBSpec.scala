package com.unibo.scalaparty.core.geometry

import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle, Polygon}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe

class AABBSpec extends AnyFlatSpec with Matchers:

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

  "penetratingVector" should "return Zero vector when AABBs are not intersecting" in:
    val a = AABB(2.0, 2.0, Point2D(0.0, 0.0))
    val b = AABB(2.0, 2.0, Point2D(5.0, 5.0))
    a penetratingVector b shouldBe None

  it should "calculate the Minimum Translation Vector (MTV) along the X axis when overlap is smaller on X" in:
    val a = AABB(4.0, 4.0, Point2D(0.0, 0.0))
    val b = AABB(4.0, 4.0, Point2D(3.0, 0.5))
    // Overlap X: (2 + 2) - 3.0 = 1.0
    // Overlap Y: (2 + 2) - 0.5 = 3.5
    // Minimum penetration vector points from A to B on X axis
    a penetratingVector b shouldBe Some(Vector2D(1.0, 0.0))

  it should "calculate the Minimum Translation Vector (MTV) along the Y axis when overlap is smaller on Y" in:
    val a = AABB(4.0, 4.0, Point2D(0.0, 0.0))
    val b = AABB(4.0, 4.0, Point2D(0.5, 3.0))
    // Overlap X: (2 + 2) - 0.5 = 3.5
    // Overlap Y: (2 + 2) - 3.0 = 1.0
    // Minimum penetration vector points from A to B on Y axis
    a penetratingVector b shouldBe Some(Vector2D(0.0, 1.0))

  it should "return a negative direction vector when target AABB is positioned to the left/bottom" in:
    val a = AABB(4.0, 4.0, Point2D(0.0, 0.0))
    val b = AABB(4.0, 4.0, Point2D(-3.0, 0.0))
    a penetratingVector b shouldBe Some(Vector2D(-1.0, 0.0))
