package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.dto.EntityDto.Spaceship as SpaceshipDto
import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.EntityType.Spaceship

object EntityAdapter:

  /** Adapts an entity ID and its associated components to an EntityDto based on its type.
   *
   *  @param entityId   the unique identifier of the entity
   *  @param components the list of components associated with the entity
   *  @return an Option containing the EntityDto if successful, or None otherwise
   */
  def toDto(entityId: EntityId, components: List[Component]): Option[EntityDto] =
    components.collectFirst { case c: EntityTypeComponent => c.entityType } match
      case Some(Spaceship) => mapSpaceship(entityId, components)
      case None => None

  private def mapSpaceship(entityId: EntityId, components: List[Component]): Option[EntityDto] =
    for
      position <- components.collectFirst { case pc: PositionComponent => pc.position }
      velocity <- components.collectFirst { case mc: MovementComponent => mc.velocity }
    yield SpaceshipDto(entityId, position, velocity)

extension (e: EntityWithComponents)
  /** Converts the entity ID and its associated components to an EntityDto based on the specified entity type.
   *  @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
   */
  def toDto: Option[EntityDto] = EntityAdapter.toDto(e._1, e._2)
