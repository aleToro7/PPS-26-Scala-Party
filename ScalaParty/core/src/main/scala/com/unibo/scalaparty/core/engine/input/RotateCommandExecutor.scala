package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.Vector2D
import com.unibo.scalaparty.core.model.GameCommand.RotateCommand

/** The RotateCommandExecutor is responsible for executing the RotateCommand, which rotates an entity's movement component by a specified angle.
 *  It implements the CommandExecutor trait for the RotateCommand type.
 */
object RotateCommandExecutor extends CommandExecutor[RotateCommand]:

  override def executeCommand(world: GameWorld, command: RotateCommand): GameWorld =
    val entityId = command.entityId
    val angle = command.angle
    val updatedWorld =
      for
        components        <- world.findComponents(entityId)
        movementComponent <- components.collectFirst { case mc: MovementComponent => mc }
        if movementComponent.velocity != Vector2D.zero
      yield world.updateComponent(entityId, rotateMovementComponent(entityId, movementComponent, angle))
    updatedWorld getOrElse world

  private def rotateMovementComponent(
      entityId: EntityId,
      component: MovementComponent,
      angle: Double
  ): MovementComponent = component.copy(velocity = component.velocity.rotated(angle))
