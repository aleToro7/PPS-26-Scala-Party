package com.unibo.scalaparty.core.model

final case class GameSettings(worldWidth: Int, spaceshipSpeed: Double)

object GameSettings:
  val default: GameSettings = GameSettings(800, 1.0)