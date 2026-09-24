package com.unibo.scalaparty.prolog

import alice.tuprolog.{InvalidTheoryException, Struct, Term, Var}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PrologEngineSpec extends AnyWordSpec with Matchers:

  private val colors = PrologEngine("color(red). color(green). color(blue).")
  private val anyColor = Struct("color", Var("C"))
  private val allColors = List("color(red)", "color(green)", "color(blue)")

  extension (solutions: LazyList[Term]) private def rendered: List[String] = solutions.map(_.toString).toList

  "A PrologEngine" should:

    "enumerate every solution of a goal, in order" in:
      colors.solve(anyColor).rendered shouldBe allColors

    "find no solution for a goal that fails" in:
      colors.solve(Struct("color", Struct("yellow"))) shouldBe empty

    "compute solutions lazily" in:
      val naturals = PrologEngine("nat(0). nat(N) :- nat(M), N is M + 1.")
      naturals.solve(Struct("nat", Var("N"))).take(3).rendered shouldBe List("nat(0)", "nat(1)", "nat(2)")

    "solve goals independently of each other" in:
      val first = colors.solve(anyColor)
      val second = colors.solve(anyColor)
      first.head.toString shouldBe "color(red)"
      second.rendered shouldBe allColors
      first.rendered shouldBe allColors

    "reject an invalid theory on creation" in:
      an[InvalidTheoryException] should be thrownBy PrologEngine("color(red")

    "reject a theory resource that does not exist" in:
      an[IllegalArgumentException] should be thrownBy PrologEngine.fromResource("theories/missing.pl")
