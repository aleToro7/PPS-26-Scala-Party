package com.unibo.scalaparty.core.ecs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class EmptyComponent() extends Component

case class AnotherComponent() extends Component

class GameWorldSpec extends AnyFlatSpec with Matchers:

  private def emptyWorld = GameWorld(Map())

  private def entityWithComponents: EntityWithComponents =
    val entityId = EntityId.generate()
    val components = List(EmptyComponent())
    (entityId, components)

  "A GameWorld" should "be empty when initialized without entities" in:
    val gameWorld = emptyWorld
    gameWorld.entities shouldBe empty

  it should "contain entities when initialized with them" in:
    val entities = List.fill(5)(entityWithComponents)
    val gameWorld = GameWorld(entities)
    gameWorld.entities should have size entities.size
    gameWorld.entities should contain theSameElementsAs entities.map(_._1)

  "addEntity" should "allow adding entities" in:
    val gameWorld = emptyWorld
    val (entityId, components) = entityWithComponents
    val updatedWorld = gameWorld.addEntity(entityId, components)
    updatedWorld.entities should contain(entityId)

  it should "replace an existing entity when adding an entity with the same ID" in:
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld + (entityId, components)
    val newComponents = List(AnotherComponent())
    val updatedWorld = gameWorld + (entityId, newComponents)
    updatedWorld.id should not be gameWorld.id
    updatedWorld.entities should contain(entityId)
    updatedWorld.findComponents(entityId) shouldBe Some(newComponents)

  "removeEntity" should "allow removing entities" in:
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld + (entityId, components)
    val updatedWorld = gameWorld.removeEntity(entityId)
    updatedWorld.entities should not contain entityId
    updatedWorld.entities shouldBe empty

  it should "not change when removing a non-existing entity" in:
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val updatedWorld = gameWorld.removeEntity(nonExistingEntityId)
    updatedWorld.entities shouldBe empty
    updatedWorld.id shouldBe gameWorld.id

  "entityWithComponents" should "return an entity's components when queried" in:
    val (entityId, components) = entityWithComponents
    val gameWorld = emptyWorld + (entityId, components)
    val retrievedComponents = gameWorld.findComponents(entityId)
    retrievedComponents shouldBe Some(components)

  it should "return None when querying components of a non-existing entity" in:
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val retrievedComponents = gameWorld.findComponents(nonExistingEntityId)
    retrievedComponents shouldBe None

  "findEntitiesWithComponent" should "return an empty list when querying entities with a specific component type and no entities have that component" in:
    val gameWorld = emptyWorld
    val retrievedEntities = gameWorld.findEntitiesWithComponent[EmptyComponent]
    retrievedEntities shouldBe empty

  it should "return the correct entities when querying entities with a specific component type" in:
    val entity1 = (EntityId.generate(), List(EmptyComponent()))
    val entity2 = (EntityId.generate(), List(EmptyComponent()))
    val entity3 = (EntityId.generate(), Nil)
    val gameWorld = GameWorld(List(entity1, entity2, entity3))
    val retrievedEntities = gameWorld.findEntitiesWithComponent[EmptyComponent]
    val expectedEntities = List(entity1, entity2)
    gameWorld.entities should have size 3
    retrievedEntities should contain theSameElementsAs expectedEntities

  "updateComponent" should "update a component when called, producing a new world" in:
    val oldComponent = EmptyComponent()
    val newComponent = AnotherComponent()
    val entity = (EntityId.generate(), List(oldComponent))
    val gameWorld = GameWorld(List(entity))
    val updatedWorld = gameWorld.updateComponent(entity._1, newComponent)
    updatedWorld.findComponents(entity._1) shouldBe Some(List(newComponent, oldComponent))
    gameWorld.id should not be updatedWorld.id

  it should "replace an existing component of the same type when called, producing a new world" in:
    case class ComponentWithId(id: Int) extends Component
    val oldComponent = ComponentWithId(1)
    val newComponent = ComponentWithId(2)
    val entity = (EntityId.generate(), List(oldComponent))
    val gameWorld = GameWorld(List(entity))
    val updatedWorld = gameWorld.updateComponent(entity._1, newComponent)
    gameWorld.id should not be updatedWorld.id
    updatedWorld.findComponents(entity._1) shouldBe Some(List(newComponent))

  it should "not return a new world called on a non-existing entity" in:
    val gameWorld = emptyWorld
    val nonExistingEntityId = EntityId.generate()
    val newComponent = EmptyComponent()
    val updatedWorld = gameWorld.updateComponent(nonExistingEntityId, newComponent)
    updatedWorld.entities shouldBe empty
    updatedWorld.id shouldBe gameWorld.id
