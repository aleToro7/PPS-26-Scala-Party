package com.unibo.scalaparty.prolog

import alice.tuprolog.{Struct, Term, Var}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TermsSpec extends AnyWordSpec with Matchers:

  private def term(text: String): Term = Term.createTerm(text)

  "Compound" should:

    "extract the name and the arguments of a compound term" in:
      term("point(1, 2)") match
        case Compound("point", Num(x), Num(y)) => (x, y) shouldBe (1.0, 2.0)
        case other => fail(s"Unexpected match on $other")

    "not match atoms and numbers" in:
      List("point", "42").foreach: text =>
        Compound.unapplySeq(term(text)) shouldBe None

  "Num" should:

    "extract the value of integer and real numbers" in:
      Num.unapply(term("42")) shouldBe Some(42.0)
      Num.unapply(term("4.5")) shouldBe Some(4.5)

    "not match non-numeric terms" in:
      List("four", "f(4)", "[4]").foreach: text =>
        Num.unapply(term(text)) shouldBe None

  "PrologList" should:

    "extract the elements of a list" in:
      PrologList.unapply(term("[a, b, c]")).map(_.map(_.toString)) shouldBe Some(List("a", "b", "c"))

    "extract no element from the empty list" in:
      PrologList.unapply(term("[]")) shouldBe Some(Nil)

    "not match non-list terms" in:
      List("a", "f(a)").foreach: text =>
        PrologList.unapply(term(text)) shouldBe None

  "The extractors" should:

    "look through the variables bound by a solution" in:
      val solution = PrologEngine("pair(X, Y) :- X = 3, Y = [1, 2].").solve(Struct("pair", Var("X"), Var("Y"))).head
      solution match
        case Compound("pair", Num(x), PrologList(elements)) =>
          x shouldBe 3.0
          elements.collect { case Num(n) => n } shouldBe List(1.0, 2.0)
        case other => fail(s"Unexpected match on $other")
