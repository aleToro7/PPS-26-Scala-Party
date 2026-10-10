package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld, ShootingComponent}
import com.unibo.scalaparty.core.model.GameCommand.LeaveCommand
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LeaveCommandExecutorSpec extends AnyFlatSpec with Matchers:
  private val leaving = EntityId.generate()
  private val staying = EntityId.generate()

  "LeaveCommandExecutor" should "remove the entity of the player who left" in:
    val world = GameWorld(List((leaving, List(ShootingComponent())), (staying, List(ShootingComponent()))))
    val updatedWorld = LeaveCommandExecutor.executeCommand(world, LeaveCommand(leaving))
    updatedWorld.findComponents(leaving) shouldBe None

  it should "leave every other entity in the world" in:
    val world = GameWorld(List((leaving, List(ShootingComponent())), (staying, List(ShootingComponent()))))
    val updatedWorld = LeaveCommandExecutor.executeCommand(world, LeaveCommand(leaving))
    updatedWorld.findComponents(staying) shouldBe Some(List(ShootingComponent()))

  it should "have no effect if the entity is no longer in the world" in:
    val world = GameWorld(List((staying, List(ShootingComponent()))))
    val updatedWorld = LeaveCommandExecutor.executeCommand(world, LeaveCommand(leaving))
    updatedWorld.entitiesWithComponents shouldBe world.entitiesWithComponents
