package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.dto.PlayerCommand
import com.unibo.scalaparty.core.dto.PlayerCommand.RotateCommand
import com.unibo.scalaparty.core.ecs.GameWorld

/** The InputGateway trait defines the interface for processing player commands and updating the game world accordingly.
 *
 *  Implementations of this trait are responsible for interpreting player commands and applying the necessary changes to the game world state.
 */
object InputGateway:

  /** Processes a list of player commands and updates the game world accordingly.
   *
   *  @param world    the current state of the game world
   *  @param commands a list of player commands to be processed
   *  @return a new instance of GameWorld reflecting the changes made by processing the commands
   */
  def processCommands(world: GameWorld, commands: List[PlayerCommand]): GameWorld =
    commands.foldLeft(world): (currentWorld, command) =>
      command match
        case rc: RotateCommand => RotateCommandExecutor.executeCommand(currentWorld, rc)
