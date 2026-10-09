package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.CollisionDetected

/** A system responsible for applying damage to the entities involved in a collision.
 *
 *  Every [[GameEvent.CollisionDetected]] event is resolved symmetrically: each entity strikes the other one, dealing
 *  its impact damage (the bullet power for projectiles, the collision damage for any other entity) if the target has
 *  health. A projectile never damages its own shooter and is consumed as soon as it damages an entity.
 *  Collision events are forwarded untouched, so that subsequent systems can still react to them.
 */
object DamageSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val updatedWorld = events.foldLeft(world):
      case (currentWorld, CollisionDetected(first, second)) => currentWorld.strike(first, second).strike(second, first)
      case (currentWorld, _) => currentWorld
    (updatedWorld, events)

  extension (world: GameWorld)

    private def strike(attackerId: EntityId, targetId: EntityId): GameWorld =
      val struckWorld =
        for
          attacker <- world.findComponents(attackerId)
          target   <- world.findComponents(targetId)
          damage   <- impactDamage(attacker, targetId)
          health   <- target.collectFirst { case hc: HealthComponent => hc }
          damagedWorld = world.updateComponent(targetId, health.damaged(damage))
        yield if isProjectile(attacker) then damagedWorld - attackerId else damagedWorld
      struckWorld.getOrElse(world)

  private def impactDamage(attacker: List[Component], targetId: EntityId): Option[Double] =
    attacker
      .collectFirst:
        case bullet: BulletComponent => Option.when(bullet.shooterId != targetId)(bullet.power)
        case CollisionDamageComponent(damage) => Some(damage)
      .flatten

  private def isProjectile(components: List[Component]): Boolean =
    components.exists(_.isInstanceOf[BulletComponent])
