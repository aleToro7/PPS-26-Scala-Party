package com.unibo.scalaparty.infrastructure.network.dto

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.syntax.*
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.MatchState
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given

class ProtocolCodecsSpec extends AnyWordSpec with Matchers:

  "ProtocolCodecs Encoders" should:

    "correctly serialize an EntityId into a JSON number" in:
      val entityId = EntityId.fromLong(42L)
      val json = entityId.asJson.noSpaces

      json shouldEqual "42"

    "correctly serialize a MatchState containing DTOs" in:
      val entityId = EntityId.fromLong(1L)
      val spaceship = EntityDto.Spaceship(entityId, Point2D(10.0, 20.0), Vector2D(1.0, 0.0))
      val state = MatchState(tick = 100L, entities = List(spaceship))

      val json = state.asJson.noSpaces

      json should include(""""tick":100""")
      json should include(""""id":1""")
      json should include(""""x":10.0""")
