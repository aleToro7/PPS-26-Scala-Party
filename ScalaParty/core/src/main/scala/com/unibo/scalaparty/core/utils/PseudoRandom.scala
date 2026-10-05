package com.unibo.scalaparty.core.utils

/** A pure pseudo-random generator: drawing a value never changes it, but yields the generator to draw the next one.
 *
 *  It is a linear congruential generator using the constants of [[java.util.Random]], so the same seed always yields
 *  the same sequence of values, keeping the game simulation deterministic and testable.
 *
 *  @param seed the current state of the generator
 */
final case class PseudoRandom(seed: Long):
  import PseudoRandom.*

  /** Draws an integer between zero (inclusive) and the given bound (exclusive).
   *
   *  @param bound the positive upper bound of the value
   *  @return the drawn value, along with the generator to draw the next value
   */
  def nextInt(bound: Int): (Int, PseudoRandom) =
    require(bound > 0, "Bound must be positive")
    val nextSeed = (seed * Multiplier + Increment) & Mask
    (((nextSeed >>> HighBitsShift) % bound).toInt, PseudoRandom(nextSeed))

  /** Draws one of the given items, each with the same probability.
   *
   *  @param items the non-empty items to draw from
   *  @tparam A the type of the items
   *  @return the drawn item, along with the generator to draw the next value
   */
  def pick[A](items: Seq[A]): (A, PseudoRandom) =
    require(items.nonEmpty, "Cannot pick from no items")
    val (index, next) = nextInt(items.size)
    (items(index), next)

object PseudoRandom:
  private val Multiplier = 0x5deece66dL
  private val Increment = 0xbL
  private val Mask = (1L << 48) - 1
  // The low bits of a linear congruential generator are poorly distributed, so only the high ones are used
  private val HighBitsShift = 17
