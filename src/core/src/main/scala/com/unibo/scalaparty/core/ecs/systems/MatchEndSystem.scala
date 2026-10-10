package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}

/** A system responsible for telling when the match is over and how it ended.
 *
 *  The match ends as soon as no spaceship is left, or as soon as a single one is left, which wins it: this holds even
 *  for a match started by a single player, which is won right away. Otherwise, it ends once the match clock reaches
 *  the time limit, a world without clock never running out of time. An elimination is reported as such even when the
 *  time limit is reached in the same update.
 *
 *  The end of the match produces a [[GameEvent.MatchEnded]] event, on every update from then on. Received events are
 *  forwarded untouched. The system must run after every other system able to remove spaceships, so that it judges
 *  the final state of the update.
 *
 *  @param settings        the rules of the match
 */
final case class MatchEndSystem(settings: MatchSettings) extends WorldSystem:

  /** @inheritdoc */
  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    (world, events ++ outcome(world).map(GameEvent.MatchEnded(_)))

  private def outcome(world: GameWorld): Option[MatchOutcome] =
    world.spaceships match
      case Nil => Some(MatchOutcome.NoSurvivors)
      case List(winner) => Some(MatchOutcome.LastStanding(winner))
      case _ => Option.when(world.clock.exists(_.elapsed >= settings.timeLimit))(MatchOutcome.TimeUp)

  extension (world: GameWorld)

    private def spaceships: List[EntityId] =
      world.entities.filter(world.findComponent[EntityTypeComponent](_).exists(_.entityType == EntityType.Spaceship))

    private def clock: Option[MatchClockComponent] =
      world.entities.view.flatMap(world.findComponent[MatchClockComponent]).headOption
