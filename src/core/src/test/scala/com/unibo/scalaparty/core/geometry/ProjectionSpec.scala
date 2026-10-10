package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ProjectionSpec extends AnyFlatSpec with Matchers:

  "A Projection" should "correctly detect overlapping intervals" in:
    val p1 = Projection(0.0, 5.0)
    val p2 = Projection(3.0, 7.0)
    p1.overlaps(p2) shouldBe true
    p2.overlaps(p1) shouldBe true

  it should "detect overlapping when touching at boundary" in:
    val p1 = Projection(0.0, 4.0)
    val p2 = Projection(4.0, 8.0)
    p1.overlaps(p2) shouldBe true
    p2.overlaps(p1) shouldBe true

  it should "detect non-overlapping intervals" in:
    val p1 = Projection(0.0, 3.0)
    val p2 = Projection(4.0, 8.0)
    p1.overlaps(p2) shouldBe false
    p2.overlaps(p1) shouldBe false

  it should "compute correct overlap amount" in:
    val p1 = Projection(0.0, 5.0)
    val p2 = Projection(3.0, 7.0)
    p1.overlap(p2) shouldBe 2.0

  it should "compute negative overlap when intervals are separated" in:
    val p1 = Projection(0.0, 2.0)
    val p2 = Projection(5.0, 8.0)
    p1.overlap(p2) shouldBe -3.0
