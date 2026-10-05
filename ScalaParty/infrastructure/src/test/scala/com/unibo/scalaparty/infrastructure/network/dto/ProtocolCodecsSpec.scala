package com.unibo.scalaparty.infrastructure.network.dto

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.geometry.Shape.{AABB, Circle}
import com.unibo.scalaparty.core.model.{MatchOutcome, MatchState}
import com.unibo.scalaparty.infrastructure.model.ServerMessage
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import io.circe.syntax.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtocolCodecsSpec extends AnyWordSpec with Matchers:

  "ProtocolCodecs Encoders" should:

    "correctly serialize an EntityId into a JSON number" in:
      val entityId = EntityId.fromLong(42L)
      val json = entityId.asJson.noSpaces

      json shouldEqual "42"

    "correctly serialize a MatchState containing DTOs" in:
      val entityId = EntityId.fromLong(1L)
      val spaceship = EntityDto.Spaceship(
        entityId,
        Point2D(10.0, 20.0),
        Vector2D(1.0, 0.0),
        AABB(2.0, 2.0, Point2D.origin),
        45.0
      )
      val state = MatchState(tick = 100L, entities = List(spaceship))

      val json = state.asJson.noSpaces

      json should include(""""tick":100""")
      json should include("""Spaceship":""")
      json should include(""""type":"AABB"""")
      json should include(""""x":10.0""")
      json should include(""""y":20.0""")
      json should include(""""rotation":45.0""")

    "tag each entity with its type so clients can distinguish them" in:
      val spaceship = EntityDto.Spaceship(
        EntityId.fromLong(1L),
        Point2D(10.0, 20.0),
        Vector2D(1.0, 0.0),
        AABB(2.0, 2.0, Point2D.origin),
        45.0
      )
      val bullet =
        EntityDto.Bullet(EntityId.fromLong(2L), Point2D(15.0, 20.0), Vector2D(100.0, 0.0), Circle(1.0, Point2D.origin))
      val state = MatchState(tick = 1L, entities = List(spaceship, bullet))
      val json = state.asJson.noSpaces

      json should include(""""Spaceship":{"id":1""")
      json should include(""""Bullet":{"id":2""")

    "tell clients which power-up a spot holds, if any" in:
      val stocked: EntityDto = EntityDto.PowerUpSpot(EntityId.fromLong(3L), Point2D(5.0, 5.0), Some("repair"))
      val recharging: EntityDto = EntityDto.PowerUpSpot(EntityId.fromLong(4L), Point2D(5.0, 5.0), None)

      stocked.asJson.noSpaces shouldEqual """{"PowerUpSpot":{"id":3,"position":{"x":5.0,"y":5.0},"powerUp":"repair"}}"""
      recharging.asJson.noSpaces shouldEqual """{"PowerUpSpot":{"id":4,"position":{"x":5.0,"y":5.0},"powerUp":null}}"""

    "tell a player starting a match which entity is its own" in:
      val message: ServerMessage = ServerMessage.MatchStarted(players = 2, you = EntityId.fromLong(7L))

      message.asJson.noSpaces shouldEqual """{"MatchStarted":{"players":2,"you":7}}"""

    "tell clients why a match ended" in:
      val ended: ServerMessage = ServerMessage.MatchEnded(MatchOutcome.TimeUp)

      ended.asJson.noSpaces shouldEqual """{"MatchEnded":{"outcome":{"TimeUp":{}}}}"""

    "tell clients that nobody survived a match" in:
      val ended: ServerMessage = ServerMessage.MatchEnded(MatchOutcome.NoSurvivors)

      ended.asJson.noSpaces shouldEqual """{"MatchEnded":{"outcome":{"NoSurvivors":{}}}}"""

    "tell clients which spaceship won a match" in:
      val ended: ServerMessage = ServerMessage.MatchEnded(MatchOutcome.LastStanding(EntityId.fromLong(2L)))

      ended.asJson.noSpaces shouldEqual """{"MatchEnded":{"outcome":{"LastStanding":{"winner":2}}}}"""
