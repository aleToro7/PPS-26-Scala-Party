package com.unibo.scalaparty.infrastructure.application

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.GameCommand
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput

object CommandAdapter:

  extension (intent: PlayerInput)

    /** Converts the raw network intent into the application domain command for the ECS.
     *
     *  @param entityId the entity the intent applies to
     *  @return the corresponding [[GameCommand]]
     */
    def toDto(entityId: EntityId): Option[GameCommand] = intent match
      case PlayerInput.Rotate(angle) => Some(GameCommand.RotateCommand(entityId, angle))
      case PlayerInput.Shoot => Some(GameCommand.ShootCommand(entityId))
