package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.{Component, EntityId, GameEvent, GameWorld, ShootingComponent}

object ShootingSystem extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): (GameWorld, Set[GameEvent]) =
    val updatedWorld = world.findEntitiesWithComponent[ShootingComponent]
      .foldLeft(world):
        case (currentWorld, (entityId, components)) =>
          val updatedShootingComponent = decreaseCooldownTimer(entityId, components, dt)
          updatedShootingComponent match
            case Some(sc) => currentWorld.updateComponent(entityId, sc)
            case None => currentWorld
    (updatedWorld, events)
            
  private def decreaseCooldownTimer(entityId: EntityId, components: List[Component], dt: Long): Option[ShootingComponent] =
    for
      shootingComponent <- components.collectFirst({ case sc: ShootingComponent => sc })
      if shootingComponent.cooldownTimer > 0
    yield shootingComponent.copy(cooldownTimer = Math.max(0, shootingComponent.cooldownTimer - dt))
    