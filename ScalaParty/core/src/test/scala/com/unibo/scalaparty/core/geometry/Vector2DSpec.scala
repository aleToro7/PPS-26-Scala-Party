package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Vector2DSpec extends AnyFlatSpec with Matchers:

  private val precision = 1e-10

  "A Vector2D" should "provide a zero vector constant at origin" in:
    Vector2D.zero shouldBe Vector2D(0.0, 0.0)

  it should "provide unit axes constants" in:
    Vector2D.unitX shouldBe Vector2D(1.0, 0.0)
    Vector2D.unitY shouldBe Vector2D(0.0, 1.0)

  it should "correctly add two vectors component-wise" in:
    val v1 = Vector2D(1.5, 2.0)
    val v2 = Vector2D(3.0, -1.0)
    (v1 + v2) shouldBe Vector2D(4.5, 1.0)

  it should "correctly subtract two vectors component-wise" in:
    val v1 = Vector2D(5.0, 7.0)
    val v2 = Vector2D(2.0, 3.0)
    (v1 - v2) shouldBe Vector2D(3.0, 4.0)

  it should "correctly multiply a vector by a scalar" in:
    val v = Vector2D(2.0, -3.5)
    val scalar = 2.0
    (v * scalar) shouldBe Vector2D(4.0, -7.0)

  it should "correctly multiply a scalar by a vector from the left" in:
    val v = Vector2D(2.0, -3.5)
    (2.0 * v) shouldBe Vector2D(4.0, -7.0)

  it should "compute the correct dot product" in:
    val v1 = Vector2D(2.0, 3.0)
    val v2 = Vector2D(4.0, -1.0)
    (v1 dot v2) shouldBe 5.0
    (Vector2D(1.0, 0.0) dot Vector2D(0.0, 1.0)) shouldBe 0.0

  it should "compute the correct 2D cross product" in:
    val v1 = Vector2D(1.0, 0.0)
    val v2 = Vector2D(0.0, 1.0)
    (v1 cross v2) shouldBe 1.0
    (v2 cross v1) shouldBe -1.0
    (v1 cross v1) shouldBe 0.0

  it should "compute a perpendicular vector rotated 90 degrees CCW" in:
    val v = Vector2D(3.0, 4.0)
    val perp = v.perpendicular
    perp shouldBe Vector2D(-4.0, 3.0)
    (v dot perp) shouldBe 0.0
    perp.module shouldBe v.module

  it should "calculate the correct module" in:
    val v = Vector2D(3.0, 4.0)
    v.module shouldBe 5.0
    v.moduleSquared shouldBe 25.0

  it should "normalize a non-zero vector to unit length" in:
    val v = Vector2D(3.0, 0.0)
    v.normalized shouldBe Vector2D(1.0, 0.0)
    v.normalized.module shouldBe 1.0

  it should "return Vector2D.zero when normalizing a zero module vector" in:
    val zeroVec = Vector2D.zero
    zeroVec.normalized shouldBe Vector2D.zero

  it should "rotate a vector by a given angle in degrees" in:
    val v = Vector2D(1.0, 0.0)
    val angle = 90.0
    val rotated: Vector2D = v.rotated(angle)
    rotated.x shouldBe 0.0 +- precision
    rotated.y shouldBe 1.0 +- precision

  it should "return the correct angle of rotation in degrees" in:
    val v = Vector2D(1, 1)
    v.angle shouldBe 45.0 +- precision
