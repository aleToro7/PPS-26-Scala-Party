package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.dto.EntityDto.{Bullet as BulletDto, Spaceship as SpaceshipDto}
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.EntityType.{Bullet, Spaceship}
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

object EntityAdapter:

  /** Adapts an entity ID and its associated components to an EntityDto based on its type.
   *
   *  @param entityId   the unique identifier of the entity
   *  @param components the list of components associated with the entity
   *  @return an Option containing the EntityDto if successful, or None otherwise
   */
  def toDto(entityId: EntityId, components: List[Component]): Option[EntityDto] =
    components.collectFirst { case c: EntityTypeComponent => c.entityType } match
      case Some(Spaceship) => extractEntity(entityId, components)(SpaceshipDto.apply)
      case Some(Bullet) => extractEntity(entityId, components)(BulletDto.apply)
      case None => None

  // TODO: this is a really weak logic that will need to be refactored as soon a the entity will have different components
  private def extractEntity[E <: EntityDto](
      entityId: EntityId,
      components: List[Component]
  )(mapper: (entityId: EntityId, position: Point2D, velocity: Vector2D) => E): Option[E] =
    for
      position <- components.collectFirst { case pc: PositionComponent => pc.position }
      velocity <- components.collectFirst { case mc: MovementComponent => mc.velocity }
    yield mapper(entityId, position, velocity)

extension (e: EntityWithComponents)
  /** Converts the entity ID and its associated components to an EntityDto based on the specified entity type.
   *  @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
   */
  def toDto: Option[EntityDto] = EntityAdapter.toDto(e._1, e._2)
