package com.unibo.scalaparty.core.model

final case class GameSettings(
 worldWidth: Int = 800,
 worldHeight: Int = 800,
 spaceshipSpeed: Double = 50.0,
 spaceshipRotationSpeed: Double = 180.0
):
  require(worldWidth > 0 && worldHeight > 0, "Arena dimensions must be positive")
  require(spaceshipSpeed > 0.0, "Spaceship speed must be positive")
  require(spaceshipRotationSpeed > 0.0, "Rotation speed must be positive")

object GameSettings:
  val default: GameSettings = GameSettings()