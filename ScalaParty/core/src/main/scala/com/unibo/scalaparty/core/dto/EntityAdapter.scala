package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.dto.EntityDto.{Bullet as BulletDto, Spaceship as SpaceshipDto}
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.EntityType.{Bullet, Spaceship}
import com.unibo.scalaparty.core.utils.collectFirstOfClass

object EntityAdapter:

  /** Adapts an entity ID and its associated components to an EntityDto based on its type.
   *
   *  @param entityId   the unique identifier of the entity
   *  @param components the list of components associated with the entity
   *  @return an Option containing the EntityDto if successful, or None otherwise
   */
  def toDto(entityId: EntityId, components: List[Component]): Option[EntityDto] =
    val entityType = components.collectFirstOfClass[EntityTypeComponent].map(_.entityType)
    entityType match
      case Some(Spaceship) => extractSpaceship(entityId, components)
      case Some(Bullet) => extractBullet(entityId, components)
      case None => None

  private def extractBullet(
      entityId: EntityId,
      components: List[Component]
  ): Option[BulletDto] =
    for
      position <- components.collectFirstOfClass[PositionComponent].map(_.position)
      velocity <- components.collectFirstOfClass[MovementComponent].map(_.velocity)
      shape    <- components.collectFirstOfClass[ShapeComponent].map(_.shape)
    yield BulletDto(entityId, position, velocity, shape)

  private def extractSpaceship(entityId: EntityId, components: List[Component]): Option[SpaceshipDto] =
    for
      position <- components.collectFirstOfClass[PositionComponent].map(_.position)
      velocity <- components.collectFirstOfClass[MovementComponent].map(_.velocity)
      shape    <- components.collectFirstOfClass[ShapeComponent].map(_.shape)
      rotation = components.collectFirstOfClass[RotationComponent].map(_.angle) getOrElse 0.0
      health = components.collectFirstOfClass[HealthComponent]
    yield SpaceshipDto(
      entityId,
      position,
      velocity,
      shape,
      rotation,
      health.map(_.current).getOrElse(100.0),
      health.map(_.max).getOrElse(100.0)
    )

extension (e: EntityWithComponents)
  /** Converts the entity ID and its associated components to an EntityDto based on the specified entity type.
   *  @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
   */
  def toDto: Option[EntityDto] = EntityAdapter.toDto(e._1, e._2)
