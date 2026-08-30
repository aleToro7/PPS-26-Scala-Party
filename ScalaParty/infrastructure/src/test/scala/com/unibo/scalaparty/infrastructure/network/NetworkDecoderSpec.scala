package com.unibo.scalaparty.infrastructure.network

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.parser.decode
import io.circe.generic.auto.*
import com.unibo.scalaparty.infrastructure.network.dto.ProtocolCodecs.given 

import com.unibo.scalaparty.core.model.PlayerCommand

class NetworkDecoderSpec extends AnyWordSpec with Matchers:

  "The JSON Decoder for PlayerCommand".should:

    "successfully decode a Rotate command".in:
      val json = """{"Rotate": {"angle": 45.0}}"""
      val result = decode[PlayerCommand](json)
      result shouldEqual Right(PlayerCommand.Rotate(45.0))

    "successfully decode a Shoot command".in:
      val json = """{"Shoot": {}}"""
      val result = decode[PlayerCommand](json)
      result shouldEqual Right(PlayerCommand.Shoot)

    "fail gracefully when JSON is structurally invalid".in:
      val json = """{ Non valid JSON }"""
      val result = decode[PlayerCommand](json)
      result.isLeft shouldEqual true

    "fail gracefully when the command is unknown".in:
      // Not valid command
      val json = """{"Teleport": {"x": 100, "y": 200}}"""
      val result = decode[PlayerCommand](json)
      result.isLeft shouldEqual true