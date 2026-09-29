package com.unibo.scalaparty.core.ecs

/** Represents an event that can occur in the game world.
 */
enum GameEvent:
  /** Represents the death of an entity in the game world.
   *
   *  @param entityId the unique identifier of the entity that has died
   */
  case Death(entityId: EntityId)

  /** Represents the spawning of an entity in the game world.
   *
   *  @param entityId the unique identifier of the entity that has been spawned
   */
  case Spawn(entityId: EntityId)

  /** Represents a collision between two entities in the game world.
   *
   *  @param entityId1 the unique identifier of the first entity involved in the collision
   *  @param entityId2 the unique identifier of the second entity involved in the collision
   */
  case CollisionDetected(entityId1: EntityId, entityId2: EntityId)
