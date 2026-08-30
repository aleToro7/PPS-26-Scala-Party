package com.unibo.scalaparty.infrastructure.application

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.unibo.scalaparty.core.dto.PlayerCommand as DtoCommand
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import CommandAdapter.*

class CommandAdapterSpec extends AnyWordSpec with Matchers:

  "CommandAdapter".should:

    "translate a Rotate intent into a Rotate DTO with the provided EntityId".in:
      val entityId = EntityId.generate()
      val intent = IntentCommand.Rotate(45.0)

      val result = intent.toDto(entityId)

      result shouldBe Some(DtoCommand.RotateCommand(entityId, 45.0))

    "return None for unsupported intents like Shoot".in:
      val entityId = EntityId.generate()
      val intent = IntentCommand.Shoot

      val result = intent.toDto(entityId)

      result shouldBe None
