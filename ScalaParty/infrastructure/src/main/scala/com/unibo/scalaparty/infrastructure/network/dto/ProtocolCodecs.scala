package com.unibo.scalaparty.infrastructure.network.dto

import io.circe.Decoder
import io.circe.generic.semiauto.*
import com.unibo.scalaparty.core.model.PlayerCommand

object ProtocolCodecs:
  given Decoder[PlayerCommand] = deriveDecoder
