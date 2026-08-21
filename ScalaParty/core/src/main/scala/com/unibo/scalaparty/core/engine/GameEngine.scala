package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{Entity, PlayerCommand}

/** A trait representing the game engine responsible for updating the state of the game world based on player commands and elapsed time. */
trait GameEngine:
  /** Updates the state of the game world based on the provided player commands and the elapsed time.
   *
   * @param list  a list of player commands to be processed
   * @param dt    the elapsed time since the last update, in milliseconds
   * @return a new list of entities representing the updated state of the game world
   */
  def update(list: List[PlayerCommand], dt: Long): List[Entity]