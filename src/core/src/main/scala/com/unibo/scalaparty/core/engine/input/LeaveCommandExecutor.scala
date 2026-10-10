package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.GameWorld
import com.unibo.scalaparty.core.model.GameCommand.LeaveCommand

/** The LeaveCommandExecutor is responsible for executing the LeaveCommand, which removes from the game world the entity
 *  of a player who left the match, so that it does not keep playing uncontrolled.
 */
object LeaveCommandExecutor extends CommandExecutor[LeaveCommand]:

  override def executeCommand(world: GameWorld, command: LeaveCommand): GameWorld =
    world.removeEntity(command.entityId)
