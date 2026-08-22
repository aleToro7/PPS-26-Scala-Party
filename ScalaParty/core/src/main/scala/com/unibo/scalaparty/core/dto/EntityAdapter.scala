package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.dto.EntityDto.Spaceship as SpaceshipDto
import com.unibo.scalaparty.core.ecs.{Component, EntityId, EntityType, EntityTypeComponent, MovementComponent, PositionComponent}
import com.unibo.scalaparty.core.ecs.EntityType.Spaceship

trait EntityMapper[T <: EntityType]:

  /** Attempts to map the given entity ID and its associated components to an EntityDto.
    * @param entityId the unique identifier of the entity
    * @param components the list of components associated with the entity
    * @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
    */
  def mapToDto(entityId: EntityId, components: List[Component]): Option[EntityDto]


object EntityMapper:

  given spaceshipMapper: EntityMapper[Spaceship.type] with
    override def mapToDto(entityId: EntityId, components: List[Component]): Option[EntityDto] = for
      position <- components.collectFirst { case pc: PositionComponent => pc.position }
      velocity <- components.collectFirst { case mc: MovementComponent => mc.velocity }
    yield SpaceshipDto(entityId, position, velocity)

object EntityAdapter:

  /** Adapts an entity ID and its associated components to an EntityDto based on the specified entity type.
    * @param entityId the unique identifier of the entity
    * @param components the list of components associated with the entity
    * @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
    */
  def toDto(entityId: EntityId, components: List[Component]): Option[EntityDto] =
    val entityType = components.collectFirst({ case c: EntityTypeComponent => c.entityType })
    entityType match
      case Some(Spaceship) =>
        summon[EntityMapper[Spaceship.type]].mapToDto(entityId, components)
      case _ => None


type EntityWithComponents = (EntityId, List[Component])

extension (e: (EntityId, List[Component]))
  /** Converts the entity ID and its associated components to an EntityDto based on the specified entity type.
    * @return an Option containing the EntityDto if the mapping is successful, or None if the mapping fails
    */
  def toDto: Option[EntityDto] = EntityAdapter.toDto(e._1, e._2)