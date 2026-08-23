package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.{EntityDto, PlayerCommand}
import com.unibo.scalaparty.core.ecs.EntityId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameEngineSpec extends AnyFlatSpec with Matchers:
    
  "A GameEngine" should "not create a new world if no command is passed" in:
    val player = EntityId.generate()
    val engine = GameEngine(player)
    val initialState = engine.update(Nil, 0)
    val newState = engine.update(Nil, 100)
    newState shouldEqual initialState