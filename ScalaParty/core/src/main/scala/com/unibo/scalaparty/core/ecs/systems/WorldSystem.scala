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

object WorldSystem:
  extension (system: WorldSystem)
    /** Composes two systems into a single system that executes them in sequence.
     *
     *  The resulting system will first execute the `system` and then execute the `nextSystem`, passing the updated
     *  game world and events from the first system to the second system.
     *
     *  @param nextSystem the system to be executed after the current system
     *  @return a new system that represents the composition of the two systems
     */
    def >>(nextSystem: WorldSystem): SystemPipeline = SystemPipeline(system, nextSystem)

/** Represents a pipeline of systems to be executed in sequence.
 *  The systems in the pipeline are executed in the order they are defined, with each system receiving the updated game world and events from the previous system.
 *  The order is very important, as it can affect the final state of the game world and the events generated.
 *
 *  @see [[WorldSystem]] for more information on how to define a system.
 */
opaque type SystemPipeline = List[WorldSystem]

object SystemPipeline:
  def apply(systems: WorldSystem*): SystemPipeline = systems.toList

  private def apply(systems: List[WorldSystem]): SystemPipeline = systems

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
   *  @param settings the game settings used to configure the systems
   *  @param players  how many players the match starts with
   *  @return a new system pipeline with the default systems
   */
  def default(settings: GameSettings, players: Int): SystemPipeline =
    ClockSystem // First, let the time of this tick pass
      >> MovementSystem // Then, move entities based on their velocity
      >> ArenaSystem(settings) // Then, check for arena boundaries
      >> CollisionSystem // Next, check for collisions between entities
      >> PowerUpSystem(settings.powerUps) // Then, pick up power-ups from the final positions, before they are used
      // The order of the following two is not important
      >> ShootingSystem
      >> DamageSystem
      >> DeathSystem // Then, remove the entities destroyed by the damage applied in this tick
      >> MatchEndSystem(settings.matchSettings, players) // Finally, judge the match on what is left of it

  extension (pipeline: SystemPipeline)

    /** Composes two systems into a single system pipeline that executes them in sequence.
     *
     *  The resulting system pipeline will first execute the `pipeline` and then execute the `nextSystem`, passing the updated
     *  game world and events from the first system to the second system.
     *  *
     *  @param nextSystem the system to be executed after the current pipeline
     *  @return a new system pipeline that represents the composition of the two systems
     */
    def >>(nextSystem: WorldSystem): SystemPipeline = pipeline :+ nextSystem

    /** Converts the system pipeline to an ordered list of systems.
     *
     *  @return a list of systems in the pipeline
     */
    def toList: List[WorldSystem] = pipeline
