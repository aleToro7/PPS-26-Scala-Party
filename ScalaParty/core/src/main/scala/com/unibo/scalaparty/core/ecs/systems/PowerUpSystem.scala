package com.unibo.scalaparty.core.ecs.systems

import scala.reflect.ClassTag

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.model.PowerUpSettings
import com.unibo.scalaparty.core.utils.collectFirstOfClass

/** A system responsible for the power-up spots of the map and the effects of the power-ups picked up from them.
 *
 *  On every update, the effects in action run down and the empty spots recharge. Then every spot holding a power-up
 *  hands it to the closest entity able to receive power-ups within the pickup radius, if any, and starts recharging:
 *  a boost starts acting on the entity, while a repair instantly restores its health. Entities able to receive
 *  power-ups are the ones with an [[ActiveEffectsComponent]]. Received events are forwarded untouched.
 *
 *  The system should run after every system moving entities, so that power-ups are picked up from the final positions
 *  of the update.
 *
 *  @param settings the rules of the power-ups
 */
final case class PowerUpSystem(settings: PowerUpSettings) extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    val updatedWorld = world
      .updatedAll[ActiveEffectsComponent](_.advanced(dt))
      .updatedAll[PowerUpSpotComponent](_.advanced(dt, settings.catalog))
      .withPowerUpsPickedUp
    (updatedWorld, events)

  extension (world: GameWorld)

    private def withPowerUpsPickedUp: GameWorld =
      world.findEntitiesWithComponent[PowerUpSpotComponent].map(_.id).foldLeft(world)(_.pickedUpFrom(_))

    private def pickedUpFrom(spotId: EntityId): GameWorld =
      val pickedUp =
        for
          spot         <- world.findComponent[PowerUpSpotComponent](spotId)
          powerUp      <- spot.powerUp
          spotPosition <- world.findComponent[PositionComponent](spotId).map(_.position)
          receiverId   <- world.closestReceiverTo(spotPosition)
        yield world.updateComponent(spotId, spot.emptied(settings.respawnDelay)).withEffect(powerUp.effect, receiverId)
      pickedUp.getOrElse(world)

    private def closestReceiverTo(spotPosition: Point2D): Option[EntityId] =
      world.findEntitiesWithComponent[ActiveEffectsComponent]
        .flatMap((id, components) => components.collectFirstOfClass[PositionComponent].map(id -> _.position))
        .map((id, position) => (id, (position - spotPosition).module))
        .filter((_, distance) => distance <= settings.pickupRadius)
        .minByOption((id, distance) => (distance, id.value)) // Ties go to the oldest entity, never to chance
        .map((id, _) => id)

    private def withEffect(effect: Effect, receiverId: EntityId): GameWorld = effect match
      case boost: Effect.Boost => world.updated[ActiveEffectsComponent](receiverId)(_.activated(boost))
      case Effect.Repair(amount) => world.updated[HealthComponent](receiverId)(_.healed(amount))

    private def updated[C <: Component: ClassTag](entityId: EntityId)(update: C => C): GameWorld =
      world.findComponent[C](entityId).fold(world)(component => world.updateComponent(entityId, update(component)))

    private def updatedAll[C <: Component: ClassTag](update: C => C): GameWorld =
      world.findEntitiesWithComponent[C].map(_.id).foldLeft(world)((current, id) => current.updated[C](id)(update))
