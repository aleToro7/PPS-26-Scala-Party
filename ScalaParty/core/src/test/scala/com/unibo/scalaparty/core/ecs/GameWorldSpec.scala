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
    val gameWorld = GameWorld(entities)
    gameWorld.entities should have size entities.size
    gameWorld.entities should contain theSameElementsAs entities.map(_._1)

  "A GameWorld" should "allow adding entities" in :
    val gameWorld = emptyWorld
    val (entityId, components) = entityWithComponents
    val updatedWorld = gameWorld.addEntity(entityId, components)
    updatedWorld.entities should contain(entityId)

  "A GameWorld" should "allow removing entities" in :
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld + (entityId, components)
    val updatedWorld = gameWorld.removeEntity(entityId)
    updatedWorld.entities should not contain entityId
    updatedWorld.entities shouldBe empty

  "A GameWorld" should "not change when removing a non-existing entity" in :
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val updatedWorld = gameWorld.removeEntity(nonExistingEntityId)
    updatedWorld.entities shouldBe empty
    updatedWorld.id shouldBe gameWorld.id

  "A GameWorld" should "return an entity's components when queried" in :
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld + (entityId, components)
    val retrievedComponents = gameWorld.findComponents(entityId)
    retrievedComponents shouldBe Some(components)

  "A GameWorld" should "return None when querying components of a non-existing entity" in :
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val retrievedComponents = gameWorld.findComponents(nonExistingEntityId)
    retrievedComponents shouldBe None

  "A GameWorld" should "return an empty list when querying entities with a specific component type and no entities have that component" in :
    val gameWorld = emptyWorld
    val retrievedEntities = gameWorld.findEntitiesWithComponent[EmptyComponent]
    retrievedEntities shouldBe empty

  "A GameWorld" should "return the correct entities when querying entities with a specific component type" in :
    val (entityId1, components1) = (EntityId.generate(), List(EmptyComponent()))
    val (entityId2, components2) = (EntityId.generate(), List(EmptyComponent()))
    val (entityId3, components3) = (EntityId.generate(), List())
    val gameWorld = GameWorld(List((entityId1, components1), (entityId2, components2), (entityId3, components3)))
    val retrievedEntities = gameWorld.findEntitiesWithComponent[EmptyComponent]
    gameWorld.entities should have size 3
    retrievedEntities should contain theSameElementsAs List(entityId1, entityId2)
