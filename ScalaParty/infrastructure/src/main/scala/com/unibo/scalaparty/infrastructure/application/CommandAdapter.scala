package com.unibo.scalaparty.infrastructure.application

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.GameCommand
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput

object CommandAdapter:

  extension (intent: PlayerInput)

    /** Converts the raw network intent into the application domain DTO for the ECS.
     *  Returns None if the command is not yet supported by the DTO.
     */
    def toDto(entityId: EntityId): Option[GameCommand] = intent match
      case PlayerInput.Rotate(angle) => Some(GameCommand.RotateCommand(entityId, angle))
      case PlayerInput.Shoot => None
