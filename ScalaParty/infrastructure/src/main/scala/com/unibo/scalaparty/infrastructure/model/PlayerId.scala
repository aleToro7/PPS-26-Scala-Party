package com.unibo.scalaparty.infrastructure.model

import java.util.UUID

opaque type PlayerId = UUID

object PlayerId:
  def random(): PlayerId = UUID.randomUUID()
  def fromString(s: String): PlayerId = UUID.fromString(s)

  extension (id: PlayerId) def value: UUID = id
