package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.{GameWorld, ShootingComponent}
import com.unibo.scalaparty.core.model.GameCommand.ShootCommand
import com.unibo.scalaparty.core.utils.collectFirstOfClass

/** The ShootCommandExecutor is responsible for executing the ShootCommand, which handles the shooting action of an entity in the game world.
 *  It is responsible to take a shoot intent from an entity and update the game world accordingly.
 */
object ShootCommandExecutor extends CommandExecutor[ShootCommand]:

  override def executeCommand(world: GameWorld, command: ShootCommand): GameWorld =
    val entityId = command.entityId
    val updatedWorld =
      for
        components        <- world.findComponents(entityId)
        shootingComponent <- components.collectFirstOfClass[ShootingComponent]
        if !shootingComponent.isShooting
      yield world.updateComponent(entityId, shootingComponent.copy(isShooting = true))
    updatedWorld getOrElse world
