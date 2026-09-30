package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*

/** A system responsible for removing the entities whose health has been depleted.
 *
 *  Every removed entity produces a [[GameEvent.Death]] event, so that subsequent systems can react to it.
 *  Entities without health are never removed. Received events are forwarded untouched.
 */
object DeathSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val destroyed = world.entities.filter(world.findComponent[HealthComponent](_).exists(_.isDepleted))
    (destroyed.foldLeft(world)(_ - _), events ++ destroyed.map(GameEvent.Death(_)))
