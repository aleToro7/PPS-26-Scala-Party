package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld, ShootingComponent}
import com.unibo.scalaparty.core.model.GameCommand.ShootCommand
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ShootCommandExecutorSpec extends AnyFlatSpec with Matchers:
  private val entityId = EntityId.generate()
  private val shootCommand: ShootCommand = ShootCommand(entityId)

  "ShootCommandExecutor" should "not update the world if the entity is not found" in:
    val world = GameWorld(Nil)
    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)
    updatedWorld.id shouldBe world.id

  "ShootCommandExecutor" should "not update the world if the entity does not have a shooting component" in:
    val entity = (entityId, Nil)
    val world = GameWorld(List(entity))
    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)
    updatedWorld.id shouldBe world.id

  "ShootCommandExecutor" should "update the world if the entity has a shooting component" in:
    val shootingComponent = ShootingComponent()
    val entity = (entityId, List(shootingComponent))
    val world = GameWorld(List(entity))

    shootingComponent.isShooting shouldBe false

    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)

    updatedWorld.id should not be world.id

    val updatedShooting = updatedWorld
      .findComponents(entityId)
      .getOrElse(Nil)
      .collectFirst { case sc: ShootingComponent => sc }

    updatedShooting.map(_.isShooting) shouldBe Some(true)

  "ShootCommandExecutor" should "not update the world if the entity has a shooting component but is already shooting" in:
    val shootingComponent = ShootingComponent(isShooting = true)
    val entity = (entityId, List(shootingComponent))
    val world = GameWorld(List(entity))

    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)

    updatedWorld.id shouldBe world.id
