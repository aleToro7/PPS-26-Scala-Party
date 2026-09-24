package com.unibo.scalaparty.prolog

import scala.util.Using

import alice.tuprolog.{Prolog, SolveInfo, Term, Theory}

/** A read-only Prolog engine answering goals against a fixed theory. */
trait PrologEngine:

  /** Solves a goal against the theory of the engine.
   *
   *  Solutions are computed lazily, one at a time as the result is traversed, so a goal with infinitely many
   *  solutions can be safely solved as long as only a finite prefix of them is consumed.
   *
   *  @param goal the goal to solve
   *  @return the goal instantiated with the bindings of each solution, in the order Prolog finds them
   */
  def solve(goal: Term): LazyList[Term]

object PrologEngine:

  /** Creates an engine from the source text of a theory.
   *
   *  @param theory the source text of the theory
   *  @return a new [[PrologEngine]] answering goals against the given theory
   *  @throws alice.tuprolog.InvalidTheoryException if the theory is not valid Prolog
   */
  def apply(theory: String): PrologEngine = TuPrologEngine(Theory(theory))

  /** Creates an engine from a theory stored as a classpath resource.
   *
   *  @param path the classpath location of the theory
   *  @return a new [[PrologEngine]] answering goals against the theory found at `path`
   *  @throws IllegalArgumentException if no resource exists at `path`
   *  @throws alice.tuprolog.InvalidTheoryException if the theory is not valid Prolog
   */
  def fromResource(path: String): PrologEngine =
    val stream = Option(getClass.getClassLoader.getResourceAsStream(path))
      .getOrElse(throw IllegalArgumentException(s"No Prolog theory found at resource '$path'"))
    TuPrologEngine(Using.resource(stream)(Theory(_)))

/** A [[PrologEngine]] backed by tuProlog.
 *
 *  tuProlog engines are stateful, as the search for further solutions resumes from the last one found: every goal is
 *  therefore solved by a dedicated engine, which keeps concurrent and interleaved resolutions independent.
 *
 *  @param theory the theory goals are solved against
 */
private class TuPrologEngine(theory: Theory) extends PrologEngine:
  // Consulting the theory upfront makes an invalid theory fail on creation rather than on the first goal.
  newProlog()

  /** @inheritdoc */
  override def solve(goal: Term): LazyList[Term] =
    val prolog = newProlog()
    solutions(prolog, prolog.solve(goal))

  private def newProlog(): Prolog =
    val prolog = Prolog()
    prolog.setTheory(theory)
    prolog

  private def solutions(prolog: Prolog, info: SolveInfo): LazyList[Term] =
    if !info.isSuccess then LazyList.empty
    else if info.hasOpenAlternatives then info.getSolution #:: solutions(prolog, prolog.solveNext())
    else LazyList(info.getSolution)
