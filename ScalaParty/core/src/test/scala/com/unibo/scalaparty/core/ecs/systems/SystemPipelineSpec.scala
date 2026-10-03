package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.Death
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.GameSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SystemPipelineSpec extends AnyFlatSpec with Matchers:

  private val defaultDt = 1_000L
  private val defaultSystems = SystemPipeline.default(GameSettings.default).toList

  private def runDefaultPipeline(world: GameWorld): SystemOutput =
    defaultSystems.foldLeft((world, Set.empty[GameEvent])):
      case ((currentWorld, events), system) => system.update(currentWorld, events, defaultDt)

  "The default SystemPipeline" should "remove the destroyed entities after damage has been applied" in:
    defaultSystems should contain inOrder (DamageSystem, DeathSystem)

  it should "remove a spaceship in the same update that a lethal hit depletes its health" in:
    val shipId = EntityId.generate()
    val power = 10.0
    val world = GameWorld(
      List(
        EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, shipId, maxHealth = power),
        EntityFactory.createBullet(EntityId.generate(), Point2D(1.0, 0.0), Vector2D.zero, power = power)
      )
    )

    val (updatedWorld, events) = runDefaultPipeline(world)

    updatedWorld.entities shouldBe empty
    events should contain(Death(shipId))
