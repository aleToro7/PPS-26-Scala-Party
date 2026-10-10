package com.unibo.scalaparty.core.model

import scala.concurrent.duration.FiniteDuration

/** A DSL declaring power-ups as readable sentences:
 *  {{{
 *  powerUp("rapid-fire") lasting 8.seconds scaling ShootCooldown by 0.5
 *  powerUp("repair") healing 30
 *  }}}
 *  Only a complete sentence yields a [[PowerUp]], so a declaration left halfway does not compile.
 */
object PowerUpDsl:
  export Stat.*

  /** Starts the declaration of a power-up.
   *
   *  @param name the unique name identifying the power-up
   *  @return the power-up whose effect is still to be declared
   */
  def powerUp(name: String): NamedPowerUp = NamedPowerUp(name)

  /** A power-up whose effect is still to be declared.
   *
   *  @param name the unique name identifying the power-up
   */
  final case class NamedPowerUp(name: String):

    /** Declares a power-up modifying a stat for some time.
     *
     *  @param duration how long the modification lasts
     *  @return the power-up whose modified stat is still to be declared
     */
    infix def lasting(duration: FiniteDuration): TimedPowerUp = TimedPowerUp(name, duration)

    /** Declares a power-up instantly restoring part of the health.
     *
     *  @param amount the health points to restore
     *  @return the declared power-up
     */
    infix def healing(amount: Double): PowerUp = PowerUp(name, Effect.Repair(amount))

  /** A power-up lasting some time, whose modified stat is still to be declared.
   *
   *  @param name     the unique name identifying the power-up
   *  @param duration how long the modification lasts
   */
  final case class TimedPowerUp(name: String, duration: FiniteDuration):

    /** Declares the stat modified by the power-up.
     *
     *  @param stat the stat to scale
     *  @return the power-up whose scaling factor is still to be declared
     */
    infix def scaling(stat: Stat): ScalingPowerUp = ScalingPowerUp(name, duration, stat)

  /** A power-up scaling a stat for some time, whose scaling factor is still to be declared.
   *
   *  @param name     the unique name identifying the power-up
   *  @param duration how long the modification lasts
   *  @param stat     the stat to scale
   */
  final case class ScalingPowerUp(name: String, duration: FiniteDuration, stat: Stat):

    /** Declares the factor scaling the stat.
     *
     *  @param factor the positive multiplier applied to the stat
     *  @return the declared power-up
     */
    infix def by(factor: Double): PowerUp = PowerUp(name, Effect.Boost(StatModifier(stat, factor), duration.toMillis))
