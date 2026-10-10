package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.geometry.{Point2D, Shape}

final case class MatchState(
    tick: Long,
    arena: Shape.AABB = Shape.AABB(800.0, 800.0, Point2D.origin),
    entities: List[EntityDto]
)
