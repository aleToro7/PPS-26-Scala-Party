package com.unibo.scalaparty.core.model

import com.unibo.scalaparty.core.dto.EntityDto

final case class MatchState(tick: Long, entities: List[EntityDto])
