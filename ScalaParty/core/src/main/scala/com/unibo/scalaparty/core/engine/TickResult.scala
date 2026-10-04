package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.GameEvent

/** What a single update of the game engine produced.
 *
 *  @param entities the entities of the game world after the update
 *  @param events   the events that occurred during the update
 */
final case class TickResult(entities: List[EntityDto], events: Set[GameEvent])
