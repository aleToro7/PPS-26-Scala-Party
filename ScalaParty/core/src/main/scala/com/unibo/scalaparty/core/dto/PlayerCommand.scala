package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.ecs.EntityId

enum PlayerCommand:
  /** Represents a command to move the player's spaceship in a specific direction.
   *
   * @param angle the angle in degrees indicating the direction of movement
   */
  case RotateCommand(entityId: EntityId, angle: Double)
