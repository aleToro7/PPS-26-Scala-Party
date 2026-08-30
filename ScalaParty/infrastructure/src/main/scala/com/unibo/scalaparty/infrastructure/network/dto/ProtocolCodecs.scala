package com.unibo.scalaparty.infrastructure.network.dto

import io.circe.*
import io.circe.generic.semiauto.*
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{GameCommand, GameEvent, MatchState}

object ProtocolCodecs:
  given Decoder[EntityId] = Decoder.decodeLong.map(EntityId.fromLong)
  given Encoder[EntityId] = Encoder.encodeLong.contramap(_.value)

  // Inbound (Client -> Server)
  given Decoder[GameCommand] = deriveDecoder

  // Outbound (Server -> Client)
  given Encoder[GameCommand] = deriveEncoder
  given Encoder[EntityDto] = deriveEncoder
  given Encoder[MatchState] = deriveEncoder
  given Encoder[GameEvent] = deriveEncoder
