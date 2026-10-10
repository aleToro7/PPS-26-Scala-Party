package com.unibo.scalaparty.infrastructure.network

import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given
import io.circe.generic.auto.*
import io.circe.parser.decode
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class NetworkDecoderSpec extends AnyWordSpec with Matchers:

  "The JSON Decoder for PlayerInput".should:

    "successfully decode a Rotate command".in:
      val json = """{"Rotate": {"angle": 45.0}}"""
      val result = decode[PlayerInput](json)
      result shouldEqual Right(PlayerInput.Rotate(45.0))

    "successfully decode a Shoot command".in:
      val json = """{"Shoot": {}}"""
      val result = decode[PlayerInput](json)
      result shouldEqual Right(PlayerInput.Shoot)

    "fail gracefully when JSON is structurally invalid".in:
      val json = """{ Non valid JSON }"""
      val result = decode[PlayerInput](json)
      result.isLeft shouldEqual true

    "fail gracefully when the command is unknown".in:
      // Not valid command
      val json = """{"Teleport": {"x": 100, "y": 200}}"""
      val result = decode[PlayerInput](json)
      result.isLeft shouldEqual true
