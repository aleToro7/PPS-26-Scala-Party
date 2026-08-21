package com.unibo.scalaparty.core.ecs

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class EmptyComponent() extends Component

class GameWorldSpec extends AnyFlatSpec with Matchers:

  "A GameWorld initialized without entities" should "have be empty" in :
    val gameWorld = GameWorld(Map())
    gameWorld.entities shouldBe empty

  "A GameWorld initialized with entities" should "contain those entities" in :
    val entities = List(EntityId.generate(), EntityId.generate())
    val entitiesWithComponents = entities.map(id => (id, List(EmptyComponent()))).toMap
    val gameWorld = GameWorld(entitiesWithComponents)
    gameWorld.entities should have size entities.size
    gameWorld.entities should contain theSameElementsAs entities

