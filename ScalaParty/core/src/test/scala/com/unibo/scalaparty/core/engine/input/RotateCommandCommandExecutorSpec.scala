package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld, MovementComponent}
import com.unibo.scalaparty.core.geometry.Vector2D
import com.unibo.scalaparty.core.model.GameCommand.RotateCommand
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RotateCommandCommandExecutorSpec extends AnyFlatSpec with Matchers:
  private val entityId = EntityId.generate()
  private val angleOfRotation = 90.0
  private val rotateCommand: RotateCommand = RotateCommand(entityId, angleOfRotation)

  "RotateCommandExecutor" should "not update the world if the entity is not found" in:
    val world = GameWorld(Nil)
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld shouldBe world

  "RotateCommandExecutor" should "not update the world if the entity does not have a movement component" in:
    val entity = (entityId, Nil) // Entity with no components
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld shouldBe world

  "RotateCommandExecutor" should "update the world if the entity has a movement component" in:
    val initialVelocity = Vector2D(1.0, 0.0)
    val expectedVelocity = Vector2D(0.0, 1.0)
    val movementComponent = MovementComponent(initialVelocity)
    val entity = (entityId, List(movementComponent))
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld should not be world
    val updatedComponents = updatedWorld.findComponents(entityId).getOrElse(Nil)
    val actualUpdatedVelocity = updatedComponents.collectFirst { case mc: MovementComponent => mc.velocity }
    actualUpdatedVelocity should not be empty
    val tolerance = 1e-9
    actualUpdatedVelocity.get.x shouldBe expectedVelocity.x +- tolerance
    actualUpdatedVelocity.get.y shouldBe expectedVelocity.y +- tolerance
