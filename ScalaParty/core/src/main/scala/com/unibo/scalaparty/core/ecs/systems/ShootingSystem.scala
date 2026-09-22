package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

object ShootingSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): (GameWorld, Set[GameEvent]) =
    val updatedWorld = world.findEntitiesWithComponent[ShootingComponent]
      .foldLeft(world):
        case (currentWorld, (entityId, components)) =>
          currentWorld
            .fireBulletIfNeeded(entityId, components, dt)
            .updateShootingComponent(entityId, components, dt)
    (updatedWorld, events)

  extension (component: ShootingComponent)

    private def canShoot(dt: Long): Boolean = component.isShooting && component.cooldownTimer - dt <= 0

    private def shoot(): ShootingComponent = component.copy(cooldownTimer = component.weapon.shootCooldown, isShooting = false)

    private def decreaseCooldownTimer(dt: Long): ShootingComponent =
      component.copy(cooldownTimer = Math.max(0, component.cooldownTimer - dt))

  extension (world: GameWorld)

    private def fireBulletIfNeeded(
        entityId: EntityId,
        components: List[Component],
        dt: Long
    ): GameWorld =
      val updatedWorld =
        for
          shootingComponent <- components.collectFirst { case sc: ShootingComponent => sc }
          positionComponent <- components.collectFirst { case pc: PositionComponent => pc }
          movementComponent <- components.collectFirst { case mc: MovementComponent => mc }
          if shootingComponent.canShoot(dt)
        yield world + getBullet(entityId, positionComponent, movementComponent, shootingComponent)
      updatedWorld getOrElse world

    private def updateShootingComponent(
        entityId: EntityId,
        components: List[Component],
        dt: Long
    ): GameWorld =
      components.collectFirst({ case sc: ShootingComponent => sc }) match
        case Some(sc) if sc.canShoot(dt) => world.updateComponent(entityId, sc.shoot())
        case Some(sc) if sc.cooldownTimer > 0 => world.updateComponent(entityId, sc.decreaseCooldownTimer(dt))
        case _ => world

  private def getBullet(
                         entityId: EntityId,
                         positionComponent: PositionComponent,
                         movementComponent: MovementComponent,
                         shootingComponent: ShootingComponent
                       ): EntityWithComponents =
    val position = positionComponent.position
    val weapon = shootingComponent.weapon
    val velocity = movementComponent.velocity
    
    val direction =
      if velocity.module > 0.0 then velocity.normalized
      else Vector2D(0.0, -1.0)

    val bulletVelocity = direction * weapon.bulletSpeed

    EntityFactory.createBullet(
      shooterId = entityId,
      position = position,
      velocity = bulletVelocity,
      power = weapon.bulletPower
    )