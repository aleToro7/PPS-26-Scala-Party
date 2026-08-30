package com.unibo.scalaparty.core.geometry

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Vector2DSpec extends AnyFlatSpec with Matchers:

  private val precision = 1e-10

  "A Vector2D" should "provide a zero vector constant at origin" in :
    Vector2D.zero shouldBe Vector2D(0.0, 0.0)

  "A Vector2D" should "correctly add two vectors component-wise" in :
    val v1 = Vector2D(1.5, 2.0)
    val v2 = Vector2D(3.0, -1.0)
    (v1 + v2) shouldBe Vector2D(4.5, 1.0)

  "A Vector2D" should "correctly subtract two vectors component-wise" in :
    val v1 = Vector2D(5.0, 7.0)
    val v2 = Vector2D(2.0, 3.0)
    (v1 - v2) shouldBe Vector2D(3.0, 4.0)

  "A Vector2D" should "correctly multiply a vector by a scalar" in :
    val v = Vector2D(2.0, -3.5)
    val scalar = 2.0
    (v * scalar) shouldBe Vector2D(4.0, -7.0)

  "A Vector2D" should "calculate the correct module" in :
    val v = Vector2D(3.0, 4.0)
    v.module shouldBe 5.0

  "A Vector2D" should "normalize a non-zero vector to unit length" in :
    val v = Vector2D(3.0, 0.0)
    v.normalized shouldBe Vector2D(1.0, 0.0)
    v.normalized.module shouldBe 1.0

  "A Vector2D" should "return Vector2D.zero when normalizing a zero module vector" in :
    val zeroVec = Vector2D.zero
    zeroVec.normalized shouldBe Vector2D.zero

  "A Vector2D" should "rotate a vector by a given angle in degrees" in :
    val v = Vector2D(1.0, 0.0)
    val angle = 90.0
    val rotated: Vector2D = v.rotated(angle)
    rotated.x shouldBe 0.0 +- precision
    rotated.y shouldBe 1.0 +- precision