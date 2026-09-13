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

    private def shoot(): ShootingComponent = component.copy(cooldownTimer = component.shootCooldown, isShooting = false)

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
          if shootingComponent.canShoot(dt)
        yield world + getBullet(entityId, positionComponent, shootingComponent)
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
      shootingComponent: ShootingComponent,
  ): EntityWithComponents =
    val PositionComponent(position) = positionComponent
    val Point2D(x, y) = position
    val ShootingComponent(power, speed, _, _, _) = shootingComponent
    val velocity = Vector2D(x, y) * speed
    EntityFactory.createBullet(entityId, position, velocity, power)
