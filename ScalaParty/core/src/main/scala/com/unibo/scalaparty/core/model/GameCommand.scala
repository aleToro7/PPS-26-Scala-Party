package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.ecs.EntityId

/** Represents a command that can be sent to the game engine to perform an action on an entity.
 */
enum GameCommand:
  /** Represents a command to rotate an entity in a specific direction.
   *  The param[[angle]] should represent the additional rotation to be applied.
   *  If the entity is moving with an angle θ, the new direction should be θ + angle.
   *
   *  @param angle the angle in degrees indicating the additional rotation to be applied
   */
  case RotateCommand(entityId: EntityId, angle: Double)
