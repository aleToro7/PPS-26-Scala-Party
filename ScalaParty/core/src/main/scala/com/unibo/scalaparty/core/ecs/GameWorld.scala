package com.unibo.scalaparty.core.ecs

import java.util.concurrent.atomic.AtomicLong
import scala.reflect.ClassTag

type EntityWithComponents = (EntityId, List[Component])

opaque type WorldId = Long

object WorldId:
  private val counter = new AtomicLong()

  /** Generates a new unique [[WorldId]].
   *
   * @return a new unique [[WorldId]]
   */
  def generate(): WorldId = counter.getAndIncrement()

/** Represents a game world in the Entity-Component-System (ECS) architecture.
 *
 * A [[GameWorld]] manages a collection of entities, each identified by a unique [[EntityId]], and their associated components.
 * It provides methods to add, remove, and query entities and their components.
 */
trait GameWorld:

  /** Retrieves the unique identifier of the game world.
   *
   * @return the unique [[WorldId]] of the game world
   */
  val id: WorldId = WorldId.generate()

  /** Retrieves the list of [[EntityId]]s currently stored in the world.
   *
   * @return a list of entity IDs present in the world
   */
  val entities: List[EntityId]

  /** Retrieves a list of tuples, each containing an [[EntityId]] and its associated list of [[Component]]s.
   *
   * @return a list of (entityId, components) pairs for all entities currently in the world
   */
  def entitiesWithComponents: List[EntityWithComponents] =
    entities.map(id => (id, findComponents(id).getOrElse(Nil)))
  /** Adds a new entity with the specified [[EntityId]] and a list of [[Component]]s to the world.
   *
   * @param entity a tuple containing the unique identifier of the entity and its associated list of components
   * @return a new instance of the world containing the added entity and its components
   */
  def addEntity(entity: EntityWithComponents): GameWorld

  /** Adds a new entity with the specified [[EntityId]] and a list of [[Component]]s to the world.
   *
   * @param entityId   the unique identifier of the entity to be added
   * @param components a list of components associated with the entity
   * @return a new instance of the world containing the added entity and its components
   */
  def addEntity(entityId: EntityId, components: List[Component]): GameWorld = addEntity((entityId, components))

  /** Alias for [[addEntity]]. */
  def +(entity: EntityWithComponents): GameWorld = addEntity(entity)

  /** Removes an entity with the specified [[EntityId]] from the world.
   *
   * @param entityId the unique identifier of the entity to be removed
   * @return a new instance of the world without the removed entity
   */
  def removeEntity(entityId: EntityId): GameWorld

  /** Alias for [[removeEntity]]. */
  def -(entityId: EntityId): GameWorld = removeEntity(entityId)

  /** Retrieves the list of [[Component]]s associated with the specified [[EntityId]].
   *
   * @param entityId the unique identifier of the entity whose components are to be retrieved
   * @return an option containing the list of components if the entity exists, or None if it does not
   */
  def findComponents(entityId: EntityId): Option[List[Component]]

  /** Retrieves the list of entities that have a component of the specified class.
   *
   * @tparam C the type of the component
   * @return a list of entity IDs and their associated components that have a component of type C
   */
  def findEntitiesWithComponent[C <: Component : ClassTag]: List[EntityWithComponents]

object GameWorld:
  /** Creates a new instance of [[GameWorld]] with the specified list of entities and their associated components.
   *
   * @param entities a list of tuples, each containing an [[EntityId]] and its associated list of [[Component]]s
   * @return a new instance of [[GameWorld]] containing the provided entities and components
   */
  def apply(entities: Map[EntityId, List[Component]]): GameWorld = new GameWorldImpl(entities)

  /** Creates a new instance of [[GameWorld]] with the specified list of entities and their associated components.
   *
   * @param list a list of tuples, each containing an [[EntityId]] and its associated list of [[Component]]s
   * @return a new instance of [[GameWorld]] containing the provided entities and components
   */
  def apply(list: List[EntityWithComponents]): GameWorld = GameWorld(list.toMap)

private class GameWorldImpl(private val entityMap: Map[EntityId, List[Component]]) extends GameWorld:

  /** @inheritdoc    */
  override val entities: List[EntityId] = entityMap.keys.toList

  /** @inheritdoc    */
  override def addEntity(entity: EntityWithComponents): GameWorld =
    new GameWorldImpl(entityMap + entity)

  /** @inheritdoc    */
  override def removeEntity(entityId: EntityId): GameWorld =
    if (entityMap.contains(entityId)) new GameWorldImpl(entityMap - entityId) else this

  /** @inheritdoc    */
  override def findComponents(entityId: EntityId): Option[List[Component]] = entityMap.get(entityId)

  /** @inheritdoc    */
  override def findEntitiesWithComponent[C <: Component : ClassTag]: List[EntityWithComponents] =
    val componentClass = implicitly[ClassTag[C]].runtimeClass
    entityMap
      .filter((_, components) => components.exists(c => componentClass.isInstance(c)))
      .toList