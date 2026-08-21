package com.unibo.scalaparty.core.ecs

import java.util.concurrent.atomic.AtomicLong
import scala.reflect.ClassTag

/** Represents a bucket that holds entities and their associated components in an Entity-Component-System (ECS) architecture.
 */
trait EntityBucket:
  /** Retrieves the list of [[EntityId]]s currently stored in the bucket.
   *
   * @return a list of entity IDs present in the bucket
   */
  val entities: List[EntityId]

  /** Adds a new entity with the specified [[EntityId]] and a list of [[Component]]s to the bucket.
   *
   * @param entityId   the unique identifier of the entity to be added
   * @param components a list of components associated with the entity
   * @return a new instance of the bucket containing the added entity and its components
   */
  def addEntity[B <: EntityBucket](entityId: EntityId, components: List[Component]): B

  /** Removes an entity with the specified [[EntityId]] from the bucket.
   *
   * @param entityId the unique identifier of the entity to be removed
   * @return a new instance of the bucket without the removed entity
   */
  def removeEntity[B <: EntityBucket](entityId: EntityId): B

  /** Retrieves the list of [[Component]]s associated with the specified [[EntityId]].
   *
   * @param entityId the unique identifier of the entity whose components are to be retrieved
   * @return an option containing the list of components if the entity exists, or None if it does not
   */
  def getComponents(entityId: EntityId): Option[List[Component]]

  /** Retrieves the list of [[EntityId]]s that have a component of the specified class.
   *
   * @param componentClass the class of the component to search for
   * @tparam C the type of the component
   * @return a list of entity IDs that have the specified component
   */
  def getEntitiesWithComponent[C <: Component](componentClass: ClassTag[C]): List[EntityId]

opaque type WorldId = Long

object WorldId:
  private val counter = new AtomicLong()

  /** Generates a new unique [[WorldId]].
   *
   * @return a new unique [[WorldId]]
   */
  def generate(): WorldId = counter.getAndIncrement()


trait GameWorld extends EntityBucket:

  /** Retrieves the unique identifier of the game world.
   *
   * @return the unique [[WorldId]] of the game world
   */
  val id: WorldId = WorldId.generate()

object GameWorld:
  /** Creates a new instance of [[GameWorld]] with the specified list of entities and their associated components.
   *
   * @param entities a list of tuples, each containing an [[EntityId]] and its associated list of [[Component]]s
   * @return a new instance of [[GameWorld]] containing the provided entities and components
   */
  def apply(entities: Map[EntityId, List[Component]]): GameWorld = new GameWorldImpl(entities)

class GameWorldImpl(private val entityMap: Map[EntityId, List[Component]]) extends GameWorld:

  /** @inheritdoc */
  override val entities: List[EntityId] = entityMap.keys.toList

  /** @inheritdoc */
  override def addEntity[B <: EntityBucket](entityId: EntityId, components: List[Component]): B = ???

  /** @inheritdoc */
  override def removeEntity[B <: EntityBucket](entityId: EntityId): B = ???

  /** @inheritdoc */
  override def getComponents(entityId: EntityId): Option[List[Component]] = ???

  /** @inheritdoc */
  override def getEntitiesWithComponent[C <: Component](componentClass: ClassTag[C]): List[EntityId] = ???