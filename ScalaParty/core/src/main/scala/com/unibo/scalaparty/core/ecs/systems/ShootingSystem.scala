package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.utils.collectFirstOfClass

/** A system responsible for firing bullets and managing the weapon cooldown of shooting entities.
 *
 *  A shoot intent is consumed on every update: if the weapon is ready a bullet is spawned in front of the shooter,
 *  otherwise the intent is discarded, so shots requested during the cooldown are ignored.
 *  The weapon is boosted by the power-up effects in action on the shooter, if any.
 */
object ShootingSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val updatedWorld = world.findEntitiesWithComponent[ShootingComponent]
      .foldLeft(world):
        case (currentWorld, (entityId, components)) =>
          components
            .collectFirst { case sc: ShootingComponent => sc }
            .fold(currentWorld)(currentWorld.updateShooter(entityId, components, _, dt))
    (updatedWorld, events)

  extension (component: ShootingComponent)

    private def isReady(dt: Long): Boolean = component.cooldownTimer - dt <= 0

    private def reloaded(weapon: Weapon): ShootingComponent =
      component.copy(isShooting = false, cooldownTimer = weapon.shootCooldown)

    private def cooledDown(dt: Long): ShootingComponent =
      component.copy(isShooting = false, cooldownTimer = Math.max(0, component.cooldownTimer - dt))

  extension (world: GameWorld)

    private def updateShooter(
        entityId: EntityId,
        components: List[Component],
        shooting: ShootingComponent,
        dt: Long
    ): GameWorld =
      val weapon = boosted(shooting.weapon, components)
      val bullet =
        if shooting.isShooting && shooting.isReady(dt) then bulletFor(entityId, components, weapon)
        else None
      bullet match
        case Some(b) => (world + b).updateComponent(entityId, shooting.reloaded(weapon))
        case None =>
          val cooled = shooting.cooledDown(dt)
          if cooled == shooting then world else world.updateComponent(entityId, cooled)

  private def boosted(weapon: Weapon, components: List[Component]): Weapon =
    components.collectFirstOfClass[ActiveEffectsComponent].fold(weapon)(weapon.boostedBy)

  private def bulletFor(
      shooterId: EntityId,
      components: List[Component],
      weapon: Weapon
  ): Option[EntityWithComponents] =
    for
      position <- components.collectFirstOfClass[PositionComponent].map(_.position)
      velocity <- components.collectFirstOfClass[MovementComponent].map(_.velocity)
      direction = velocity.normalized
    yield EntityFactory.createBullet(
      shooterId = shooterId,
      position = position + direction * weapon.muzzleOffset,
      velocity = direction * weapon.bulletSpeed,
      power = weapon.bulletPower
    )
