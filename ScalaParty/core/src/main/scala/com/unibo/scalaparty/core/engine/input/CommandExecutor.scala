package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.GameWorld
import com.unibo.scalaparty.core.model.GameCommand

/** The CommandExecutor trait defines the interface for executing player commands and updating the game world accordingly.
 *  Implementations of this trait are responsible for interpreting player commands and applying the necessary changes to the game world state.
 *
 *  @tparam C the type of PlayerCommand that this executor can handle
 */
trait CommandExecutor[C <: GameCommand]:

  /** Executes a single player command and updates the game world accordingly.
   *
   *  @param world   the current state of the game world
   *  @param command the player command to be executed
   *  @return a new instance of GameWorld reflecting the changes made by executing the command
   */
  def executeCommand(world: GameWorld, command: C): GameWorld
