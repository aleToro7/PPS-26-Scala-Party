package com.unibo.scalaparty.infrastructure.model

import java.util.UUID

opaque type MatchId = UUID

object MatchId:
  def random(): MatchId = UUID.randomUUID()
  def fromString(s: String): MatchId = UUID.fromString(s)

  extension (id: MatchId) def value: UUID = id
