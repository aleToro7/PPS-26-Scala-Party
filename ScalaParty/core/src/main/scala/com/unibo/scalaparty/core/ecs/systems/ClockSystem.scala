package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.utils.collectFirstOfClass

/** A system responsible for keeping track of how long the match has been going on.
 *
 *  Every clock in the world is advanced by the time elapsed since the last update, so that subsequent systems can
 *  read the simulated time from the world itself. Received events are forwarded untouched.
 */
object ClockSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val updatedWorld = world.findEntitiesWithComponent[MatchClockComponent].foldLeft(world):
      case (currentWorld, (clockId, components)) =>
        components
          .collectFirstOfClass[MatchClockComponent]
          .fold(currentWorld)(clock => currentWorld.updateComponent(clockId, clock.advanced(dt)))
    (updatedWorld, events)
