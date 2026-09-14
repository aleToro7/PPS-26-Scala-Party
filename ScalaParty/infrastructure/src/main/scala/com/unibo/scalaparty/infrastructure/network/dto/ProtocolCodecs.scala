package com.unibo.scalaparty.infrastructure.network.dto

import io.circe.*
import io.circe.generic.semiauto.*
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{GameCommand, GameEvent, MatchState}
import com.unibo.scalaparty.infrastructure.model.ServerMessage

/** Defines Circe JSON encoders and decoders for inbound and outbound network payloads,
 *  enabling seamless serialization and deserialization across the client-server boundary.
 */
object ProtocolCodecs:

  /** Custom decoder mapping a JSON number to an [[EntityId]]. */
  given Decoder[EntityId] = Decoder.decodeLong.map(EntityId.fromLong)

  /** Custom encoder mapping an [[EntityId]] to a JSON number via its underlying value. */
  given Encoder[EntityId] = Encoder.encodeLong.contramap(_.value)

  // Inbound (Client -> Server)

  /** Decoder for incoming player and game commands sent from the client. */
  given Decoder[GameCommand] = deriveDecoder

  // Outbound (Server -> Client)

  /** Encoder for entity Data Transfer Objects representing world entities. */
  given Encoder[GameCommand] = deriveEncoder

  /** Encoder for entity Data Transfer Objects representing world entities. */
  given Encoder[EntityDto] = deriveEncoder

  /** Encoder for the authoritative match state broadcasted to clients. */
  given Encoder[MatchState] = deriveEncoder

  /** Encoder for discrete game events occurring during execution. */
  given Encoder[GameEvent] = deriveEncoder

  /** Encoder for server-to-player lobby and queue notifications. */
  given Encoder[ServerMessage] = deriveEncoder
