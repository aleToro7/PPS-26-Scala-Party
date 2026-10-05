package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.{GameEvent, GameWorld}
import com.unibo.scalaparty.core.model.GameSettings

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
trait WorldSystem:

  /** Updates the state of the game world based on the provided events and the elapsed time.
   *
   *  @param world  the current state of the game world
   *  @param events a set of game events to be processed
   *  @param dt     the elapsed time since the last update, in milliseconds
   *  @return a tuple containing the updated game world and a set of new game events generated during the update
   */
  def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput

  /** Composes two systems into a single system that executes them in sequence.
   *
   *  The resulting system will first execute the `system` and then execute the `nextSystem`, passing the updated
   *  game world and events from the first system to the second system.
   *
   *  @param next the system to be executed after the current system
   *  @return a new system that represents the composition of the two systems
   */
  def compose(next: WorldSystem): WorldSystem = (world, events, dt) =>
    val (nextWorld, nextEvents) = this.update(world, events, dt)
    next.update(nextWorld, nextEvents, dt)

  /** Alias for the [[compose]] method. */
  def >>(next: WorldSystem): WorldSystem = this.compose(next)

object WorldSystem:

  /** Creates a default system pipeline based on the provided game settings.
   *
   *  The default pipeline includes the following systems in order:
   *  1. ClockSystem: Keeps track of how long the match has been going on.
   *  2. MovementSystem: Moves entities based on their velocity.
   *  3. ArenaSystem: Checks for arena boundaries and handles entities that go out of bounds.
   *  4. CollisionSystem: Checks for collisions between entities and generates collision events.
   *  5. PowerUpSystem: Hands the power-ups to the spaceships picking them up and runs down their effects.
   *  6. ShootingSystem: Processes shooting events and updates the state of projectiles.
   *  7. DamageSystem: Applies damage to entities based on collision and shooting events.
   *  8. DeathSystem: Removes the entities whose health has been depleted.
   *  9. MatchEndSystem: Tells when the match is over and how it ended.
   *
   *  @param settings the game configuration used to set up the systems
   *  @param players  how many players the match starts with
   *  @return a new system pipeline with the default systems
   */
  def defaultPipeline(settings: GameSettings): WorldSystem =
    ClockSystem // First, let the time of this tick pass
      >> MovementSystem // Then, move entities based on their velocity
      >> ArenaSystem(settings) // Then, check for arena boundaries
      >> CollisionSystem // Next, check for collisions between entities
      >> PowerUpSystem(settings.powerUps) // Then, pick up power-ups from the final positions, before they are used
      >> ShootingSystem
      >> DamageSystem
      >> DeathSystem
      // Needs to be executed at the end,
      // it could completely erase the world and the events if the match is over.
      >> MatchEndSystem(settings.matchSettings)
