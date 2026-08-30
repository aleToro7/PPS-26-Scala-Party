package com.unibo.scalaparty.infrastructure.application

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.core.dto.PlayerCommand as DtoCommand

object CommandAdapter:

  extension (intent: IntentCommand)

    /** Converts the raw network intent into the application domain DTO for the ECS.
     *  Returns None if the command is not yet supported by the DTO.
     */
    def toDto(entityId: EntityId): Option[DtoCommand] = intent match
      case IntentCommand.Rotate(angle) => Some(DtoCommand.RotateCommand(entityId, angle))
      case IntentCommand.Shoot         => None