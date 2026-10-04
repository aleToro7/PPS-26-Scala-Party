package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.systems.SystemPipeline
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.GameCommand.LeaveCommand
import com.unibo.scalaparty.core.model.GameSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MultiplayerGameEngineSpec extends AnyFlatSpec with Matchers:

  private val settings = GameSettings.default
  private val arena = settings.map.dimension
  // The arena is centered on the origin.
  private val center = Point2D.origin

  /** The spaceships of a match of the given players, as they are before anything moves. */
  private def spawnedShips(players: List[EntityId]): List[EntityDto.Spaceship] =
    GameEngine(GameConfig(settings, players), SystemPipeline())
      .update(Nil, 0L)
      .collect { case ship: EntityDto.Spaceship => ship }

  "A GameEngine with several players" should "spawn one spaceship for each of them, with their own ids" in:
    val players = List.fill(4)(EntityId.generate())
    spawnedShips(players).map(_.id) should contain theSameElementsAs players

  it should "place the spaceships apart from each other, inside the arena" in:
    val positions = spawnedShips(List.fill(4)(EntityId.generate())).map(_.position)
    positions.distinct should have size 4
    all(positions.map(_.x.abs)) should be < arena.width / 2.0
    all(positions.map(_.y.abs)) should be < arena.height / 2.0

  it should "start every spaceship facing the center of the arena" in:
    spawnedShips(List.fill(3)(EntityId.generate())).foreach: ship =>
      val towardsCenter = (center - ship.position).normalized
      // Both where the spaceship goes and where it points, which is what collisions are checked against.
      for heading <- List(ship.velocity.normalized, Vector2D(1, 0).rotated(ship.rotation)) do
        heading.x shouldBe towardsCenter.x +- 1e-9
        heading.y shouldBe towardsCenter.y +- 1e-9

  it should "remove the spaceship of a player who left, keeping the others" in:
    val players @ List(leaving, staying) = List.fill(2)(EntityId.generate()): @unchecked
    val engine = GameEngine(GameConfig(settings, players), SystemPipeline())
    val ids = engine.update(List(LeaveCommand(leaving)), 0L).collect { case ship: EntityDto.Spaceship => ship.id }
    ids shouldBe List(staying)
