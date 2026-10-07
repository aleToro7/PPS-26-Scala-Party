package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.{Death, MatchEnded}
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchSettings}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MatchEndSystemSpec extends AnyFlatSpec with Matchers:

  private val defaultDt = 16L
  private val timeLimit = 1_000L

  private val clockId = EntityId.generate()
  private val first = EntityId.generate()
  private val second = EntityId.generate()
  private val third = EntityId.generate()
  private val bulletId = EntityId.generate()

  /** A match started by three players, all of them still alive. */
  private val world = GameWorld(
    List(
      (clockId, List(MatchClockComponent())),
      spaceship(first),
      spaceship(second),
      spaceship(third),
      EntityFactory.createBullet(first, Point2D.origin, Vector2D.zero, power = 10.0, bulletId)
    )
  )

  private val system = MatchEndSystem(MatchSettings(timeLimit))

  private def spaceship(entityId: EntityId): EntityWithComponents =
    EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, entityId)

  private def endOf(world: GameWorld, system: MatchEndSystem = system): Option[MatchOutcome] =
    system.update(world, Set.empty, defaultDt)._2.collectFirst { case MatchEnded(outcome) => outcome }

  extension (world: GameWorld)
    private def at(elapsed: Long): GameWorld = world.updateComponent(clockId, MatchClockComponent(elapsed))
    private def without(entityIds: EntityId*): GameWorld = entityIds.foldLeft(world)(_ - _)

  "MatchEndSystem" should "keep a match going before its time limit" in:
    endOf(world.at(timeLimit - 1)) shouldBe None

  it should "end a match once its time limit is reached" in:
    endOf(world.at(timeLimit)) shouldBe Some(MatchOutcome.TimeUp)

  it should "never end a match by time in a world without clock" in:
    endOf(world.without(clockId)) shouldBe None

  it should "end a match once no spaceship is left" in:
    endOf(world.without(first, second, third)) shouldBe Some(MatchOutcome.NoSurvivors)

  it should "tell that no spaceship is left even when the time limit is reached too" in:
    endOf(world.at(timeLimit).without(first, second, third)) shouldBe Some(MatchOutcome.NoSurvivors)

  it should "keep a match going while several spaceships are left" in:
    endOf(world.without(third)) shouldBe None

  it should "declare the last spaceship left the winner" in:
    endOf(world.without(second, third)) shouldBe Some(MatchOutcome.LastStanding(first))

  it should "declare the winner even when the time limit is reached too" in:
    endOf(world.at(timeLimit).without(second, third)) shouldBe Some(MatchOutcome.LastStanding(first))

  it should "leave the world untouched" in:
    val endedWorld = world.without(first, second, third)

    val (updatedWorld, _) = system.update(endedWorld, Set.empty, defaultDt)

    updatedWorld shouldBe endedWorld

  it should "forward the received events" in:
    val death = Death(third)

    val (_, events) = system.update(world.without(second, third), Set(death), defaultDt)

    events shouldBe Set(death, MatchEnded(MatchOutcome.LastStanding(first)))
