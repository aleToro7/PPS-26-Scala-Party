package com.unibo.scalaparty.core.engine.input

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.Vector2D
import com.unibo.scalaparty.core.model.GameCommand.RotateCommand
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RotateCommandExecutorSpec extends AnyFlatSpec with Matchers:
  private val tolerance = 1e-9
  private val entityId = EntityId.generate()
  private val angleOfRotation = 90.0
  private val rotateCommand: RotateCommand = RotateCommand(entityId, angleOfRotation)

  private def spawnEntity(velocity: Vector2D = Vector2D(1.0, 0.0), rotation: Double = 0.0): EntityWithComponents =
    val movementComponent = MovementComponent(velocity)
    val rotationComponent = RotationComponent(rotation)
    (entityId, List(movementComponent, rotationComponent))

  "RotateCommandExecutor" should "not update the world if the entity is not found" in:
    val world = GameWorld(Nil)
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld shouldBe world

  it should "not update the world if the entity does not have a movement component" in:
    val entity = (entityId, Nil) // Entity with no components
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld shouldBe world

  it should "update the world if the entity has a movement component" in:
    val initialVelocity = Vector2D(1.0, 0.0)
    val entity = spawnEntity(velocity = initialVelocity)
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld should not be world
    val actualUpdatedVelocity = updatedWorld.findComponent[MovementComponent](entityId).map(_.velocity)
    actualUpdatedVelocity should not be empty
    val expectedVelocity = initialVelocity.rotated(angleOfRotation)
    actualUpdatedVelocity.get.x shouldBe expectedVelocity.x +- tolerance
    actualUpdatedVelocity.get.y shouldBe expectedVelocity.y +- tolerance

  it should "rotate the movement component correctly for" in:
    val initialVelocity = Vector2D(1.0, 0.0)
    val entity = spawnEntity(initialVelocity)
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    val expectedRotation = angleOfRotation
    val updatedRotation = updatedWorld.findComponent[RotationComponent](entityId).map(_.angle)
    updatedRotation.value shouldBe expectedRotation

  it should "handle rotation of an entity with zero velocity" in:
    val entity = spawnEntity(velocity = Vector2D.zero)
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld should not be world
    val updatedRotation = updatedWorld.findComponent[RotationComponent](entityId).map(_.angle)
    updatedRotation.value shouldBe angleOfRotation

  it should "handle rotation of an entity with an angle > 360 degrees" in:
    val angle = 720.0
    val initialVelocity = Vector2D(1.0, 0.0)
    val entity = spawnEntity(velocity = initialVelocity)
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, RotateCommand(entityId, angle))
    val actualUpdatedVelocity = updatedWorld.findComponent[MovementComponent](entityId).map(_.velocity)
    actualUpdatedVelocity.value.x shouldBe initialVelocity.x +- tolerance
    actualUpdatedVelocity.value.y shouldBe initialVelocity.y +- tolerance
    val updatedRotation = updatedWorld.findComponent[RotationComponent](entityId).map(_.angle)
    updatedRotation.value shouldBe 0.0 +- tolerance

  it should "not update the world if the entity does not have nor a movement component nor a rotation component" in:
    val entity = (entityId, Nil)
    val world = GameWorld(List(entity))
    val updatedWorld = RotateCommandExecutor.executeCommand(world, rotateCommand)
    updatedWorld shouldBe world
