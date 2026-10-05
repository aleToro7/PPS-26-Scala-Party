package com.unibo.scalaparty.core.utils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PseudoRandomSpec extends AnyFlatSpec with Matchers:

  private val draws = 1_000

  private def drawMany(random: PseudoRandom, bound: Int): List[Int] =
    List.unfold((random, draws)):
      case (_, 0) => None
      case (current, left) =>
        val (value, next) = current.nextInt(bound)
        Some((value, (next, left - 1)))

  "A PseudoRandom" should "always draw the same value from the same seed" in:
    PseudoRandom(42L).nextInt(100) shouldBe PseudoRandom(42L).nextInt(100)

  it should "draw values between zero and the bound" in:
    all(drawMany(PseudoRandom(42L), 5)) should (be >= 0 and be < 5)

  it should "eventually draw every value below the bound" in:
    drawMany(PseudoRandom(42L), 5).toSet shouldBe Set(0, 1, 2, 3, 4)

  it should "reject a non-positive bound" in:
    an[IllegalArgumentException] should be thrownBy PseudoRandom(42L).nextInt(0)

  it should "pick one of the given items" in:
    val items = List("a", "b", "c")
    val (picked, _) = PseudoRandom(42L).pick(items)
    items should contain(picked)

  it should "refuse to pick from no items" in:
    an[IllegalArgumentException] should be thrownBy PseudoRandom(42L).pick(Nil)
