package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.{GameEvent, GameWorld}

/** Represents the output of a system update, consisting of the updated game world and the input game events along with any new game events generated.
 *
 *  @param world  the updated state of the game world after processing events
 *  @param events the set of game events that were processed, including any new events generated during the update
 */
type SystemOutput = (GameWorld, Set[GameEvent])

/** Represents a system that can update the state of the game world based on events and elapsed time.
 *
 *  A system is responsible for processing game events and updating the game world accordingly. It takes the current
 *  state of the game world, a set of game events, and the elapsed time since the last update, and produces an updated
 *  game world along with any new game events generated during the update.
 */
trait System:

  /** Updates the state of the game world based on the provided events and the elapsed time.
   *
   *  @param world  the current state of the game world
   *  @param events a set of game events to be processed
   *  @param dt     the elapsed time since the last update, in milliseconds
   *  @return a tuple containing the updated game world and a set of new game events generated during the update
   */
  def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput
