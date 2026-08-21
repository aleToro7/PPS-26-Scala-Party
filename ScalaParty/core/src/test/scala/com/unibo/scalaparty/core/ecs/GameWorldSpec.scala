package com.unibo.scalaparty.core.ecs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class EmptyComponent() extends Component

class GameWorldSpec extends AnyFlatSpec with Matchers:

  private def emptyWorld = GameWorld(Map())

  private def entityWithComponents =
    val entityId = EntityId.generate()
    val components = List(EmptyComponent())
    (entityId, components)

  "A GameWorld initialized without entities" should "have be empty" in :
    val gameWorld = emptyWorld
    gameWorld.entities shouldBe empty

  "A GameWorld initialized with entities" should "contain those entities" in :
    val entities = List.fill(5)(entityWithComponents)
    val gameWorld = GameWorld(entities.toMap)
    gameWorld.entities should have size entities.size
    gameWorld.entities should contain theSameElementsAs entities.map(_._1)

  "A GameWorld" should "allow adding entities" in :
    val gameWorld = emptyWorld
    val (entityId, components) = entityWithComponents
    val updatedWorld = gameWorld.addEntity(entityId, components)
    updatedWorld.entities should contain(entityId)

  "A GameWorld" should "allow removing entities" in :
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld.addEntity(entityId, components)
    val updatedWorld = gameWorld.removeEntity(entityId)
    updatedWorld.entities should not contain entityId
    updatedWorld.entities shouldBe empty

  "A GameWorld" should "not change when removing a non-existing entity" in :
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val updatedWorld = gameWorld.removeEntity(nonExistingEntityId)
    updatedWorld.entities shouldBe empty
    updatedWorld.id shouldBe gameWorld.id
