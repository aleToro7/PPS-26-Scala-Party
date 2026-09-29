package com.unibo.scalaparty.infrastructure.network.dto

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.geometry.{Point2D, Shape}
import com.unibo.scalaparty.core.model.{GameCommand, GameEvent, MatchState}
import com.unibo.scalaparty.infrastructure.model.ServerMessage
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.EncoderOps

/** Defines Circe JSON encoders and decoders for inbound and outbound network payloads,
 *  enabling seamless serialization and deserialization across the client-server boundary.
 */
object ProtocolCodecs:

  /** Custom decoder mapping a JSON number to an [[EntityId]]. */
  given Decoder[EntityId] = Decoder.decodeLong.map(EntityId.fromLong)

  /** Custom encoder mapping an [[EntityId]] to a JSON number via its underlying value. */
  given Encoder[EntityId] = Encoder.encodeLong.contramap(_.value)

  /** Custom encoder mapping a [[Point2D]] to a JSON object with "x" and "y" fields. */
  given Encoder[Point2D] = Encoder.instance: point =>
    Json.obj(
      "x" -> point.x.asJson,
      "y" -> point.y.asJson
    )

  /** Custom encoder for [[Shape]] instances. */
  given Encoder[Shape] = Encoder.instance:
    case Shape.Circle(radius, center) =>
      Json.obj(
        "type" -> "Circle".asJson,
        "radius" -> radius.asJson,
        "center" -> center.asJson
      )
    case Shape.AABB(width, height, center) =>
      Json.obj(
        "type" -> "AABB".asJson,
        "width" -> width.asJson,
        "height" -> height.asJson,
        "center" -> center.asJson
      )
    case Shape.Polygon(vertices*) =>
      Json.obj(
        "type" -> "Polygon".asJson,
        "vertices" -> vertices.toList.asJson
      )

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
