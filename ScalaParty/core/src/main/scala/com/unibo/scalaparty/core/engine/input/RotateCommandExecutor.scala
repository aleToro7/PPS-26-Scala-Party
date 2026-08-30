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
        components <- world.findComponents(entityId)
        velocity   <- components.collectFirst { case mc: MovementComponent => mc.velocity }
        if velocity != Vector2D.zero
        updatedEntity <- rotateMovementComponent(entityId, components, angle)
      yield world + updatedEntity
    updatedWorld.getOrElse(world)

  private def rotateMovementComponent(
      entityId: EntityId,
      components: List[Component],
      angle: Double
  ): Option[EntityWithComponents] =
    for
      velocity <- components.collectFirst { case mc: MovementComponent => mc.velocity }
      if velocity != Vector2D.zero
      updatedComponents = components.map:
        case mc: MovementComponent => mc.copy(velocity = velocity.rotated(angle))
        case other => other
    yield (entityId, updatedComponents)
