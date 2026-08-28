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
