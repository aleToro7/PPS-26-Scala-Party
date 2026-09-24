package com.unibo.scalaparty.prolog

import scala.jdk.CollectionConverters.*

import alice.tuprolog.{Number as PrologNumber, Struct, Term}

// Extractors to pattern match on tuProlog terms. Each of them looks through bound variables, as the terms of a
// solution may be variables standing for the actual values.

/** Matches a compound term, extracting its functor name and arguments, e.g. `Compound("point", x, y)`. */
object Compound:
  def unapplySeq(term: Term): Option[(String, Seq[Term])] = term.getTerm match
    case struct: Struct if struct.isCompound => Some((struct.getName, (0 until struct.getArity).map(struct.getArg)))
    case _ => None

/** Matches a number, either integer or real, extracting its value. */
object Num:
  def unapply(term: Term): Option[Double] = term.getTerm match
    case number: PrologNumber => Some(number.doubleValue)
    case _ => None

/** Matches a list, extracting its elements. */
object PrologList:
  def unapply(term: Term): Option[List[Term]] = term.getTerm match
    case struct: Struct if struct.isList => Some(struct.listIterator.asScala.toList)
    case _ => None
