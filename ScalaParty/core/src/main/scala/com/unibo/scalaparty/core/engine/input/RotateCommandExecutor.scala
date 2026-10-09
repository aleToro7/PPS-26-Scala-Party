package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.model.GameCommand.RotateCommand

/** The RotateCommandExecutor is responsible for executing the RotateCommand, which rotates an entity's movement component by a specified angle.
 *  It implements the CommandExecutor trait for the RotateCommand type.
 */
object RotateCommandExecutor extends CommandExecutor[RotateCommand]:

  override def executeCommand(world: GameWorld, command: RotateCommand): GameWorld =
    val RotateCommand(entityId, angle) = command
    world
      .rotateEntity(entityId, angle)
      .updateEntityVelocity(entityId, angle)

extension (world: GameWorld)

  private def updateEntityVelocity(entityId: EntityId, angle: Double): GameWorld =
    world.findComponent[MovementComponent](entityId) match
      case Some(MovementComponent(velocity)) =>
        world.updateComponent(entityId, MovementComponent(velocity.rotated(angle)))
      case None => world

  private def rotateEntity(entityId: EntityId, angle: Double): GameWorld =
    world.findComponent[RotationComponent](entityId) match
      case Some(RotationComponent(currentAngle)) =>
        val newAngle = (currentAngle + angle) % 360.0
        world.updateComponent(entityId, RotationComponent(newAngle))
      case None => world
