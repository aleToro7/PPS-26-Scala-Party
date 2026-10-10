package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.Death
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ClockSystemSpec extends AnyFlatSpec with Matchers:

  private val defaultDt = 16L

  private val clockId = EntityId.generate()
  private val shipId = EntityId.generate()

  private val world = GameWorld(
    List(
      (clockId, List(MatchClockComponent())),
      EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, shipId)
    )
  )

  private def updateWorld(world: GameWorld, events: GameEvent*): (GameWorld, Set[GameEvent]) =
    ClockSystem.update(world, events.toSet, defaultDt)

  extension (world: GameWorld)
    private def elapsed: Option[Long] = world.findComponent[MatchClockComponent](clockId).map(_.elapsed)

  "ClockSystem" should "advance the clock by the elapsed time" in:
    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.elapsed shouldBe Some(defaultDt)

  it should "accumulate the elapsed time over several updates" in:
    val (once, _) = updateWorld(world)
    val (twice, _) = updateWorld(once)

    twice.elapsed shouldBe Some(2 * defaultDt)

  it should "reject a negative elapsed time" in:
    an[IllegalArgumentException] should be thrownBy ClockSystem.update(world, Set.empty, -1L)

  it should "leave the other entities untouched" in:
    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.findComponents(shipId) shouldBe world.findComponents(shipId)

  it should "not modify a world without clocks" in:
    val worldWithoutClock = world - clockId

    val (updatedWorld, _) = updateWorld(worldWithoutClock)

    updatedWorld shouldBe worldWithoutClock

  it should "forward the received events" in:
    val death = Death(shipId)

    val (_, events) = updateWorld(world, death)

    events shouldBe Set(death)
