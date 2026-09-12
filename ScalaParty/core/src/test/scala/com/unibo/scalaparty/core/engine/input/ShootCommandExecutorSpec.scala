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
    val shootingComponent = ShootingComponent(0, 0, 0)
    val entity = (entityId, List(shootingComponent))
    val world = GameWorld(List(entity))
    shootingComponent.isShooting shouldBe false
    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)
    updatedWorld.id should not be world.id
    val updatedComponents = updatedWorld.findComponents(entityId).getOrElse(Nil)
    updatedComponents should not be empty
    val updatedShootingComponent = updatedComponents.collectFirst { case sc: ShootingComponent => sc }
    updatedShootingComponent should not be empty
    updatedShootingComponent.get.isShooting shouldBe true

  "ShootCommandExecutor" should "not update the world if the entity has a shooting component but is already shooting" in:
    val shootingComponent = ShootingComponent(0, 0, 0, isShooting = true)
    val entity = (entityId, List(shootingComponent))
    val world = GameWorld(List(entity))
    val updatedWorld = ShootCommandExecutor.executeCommand(world, shootCommand)
    updatedWorld.id shouldBe world.id
