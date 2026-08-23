package com.unibo.scalaparty.core.engine

import com.unibo.scalaparty.core.ecs.EntityId

case class GameConfig(
    players: List[EntityId],
    worldWidth: Int,
    worldHeight: Int,
    spaceshipSpeed: Double,
    spaceshipRotationSpeed: Double
)

case class SinglePlayerConfig(
    player: EntityId,
    worldWidth: Int,
    worldHeight: Int,
    spaceshipSpeed: Double,
    spaceshipRotationSpeed: Double
)
