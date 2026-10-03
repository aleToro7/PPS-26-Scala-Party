package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected

/** A system responsible for removing the entities whose health has been depleted.
 *
 *  Every removed entity produces a [[GameEvent.Death]] event, so that subsequent systems can react to it.
 *  Entities without health are never removed. Received events are forwarded untouched.
 */
object DeathSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val deadEntities = world.entities.filter(world.findComponent[HealthComponent](_).exists(_.isDepleted))
    val bullets = world.findEntitiesWithComponent[BulletComponent].map(_.id).toSet
    val destroyedBullets = events.collect { case e: CollisionDetected => e }
      .flatMap { collision => collision.entityId1 :: collision.entityId2 :: Nil }
      .intersect(bullets)
    val destroyed = deadEntities ++ destroyedBullets
    (destroyed.foldLeft(world)(_ - _), events ++ deadEntities.map(GameEvent.Death(_)))
