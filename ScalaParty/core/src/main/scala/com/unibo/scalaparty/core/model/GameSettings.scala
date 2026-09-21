package com.unibo.scalaparty.core.model

final case class ArenaSettings(
                                width: Int = 800,
                                height: Int = 800
                              ):
  require(width > 0 && height > 0, "Arena dimensions must be positive")

final case class SpaceshipSettings(
                                    speed: Double = 50.0,
                                    rotationSpeed: Double = 180.0
                                  ):
  require(speed > 0.0, "Spaceship speed must be positive")
  require(rotationSpeed > 0.0, "Rotation speed must be positive")

final case class ShootingSettings(
                                   bulletPower: Double = 10.0,
                                   bulletSpeed: Double = 100.0,
                                   shootCooldown: Long = 250L
                                 ):
  require(bulletPower > 0.0, "Bullet power must be positive")
  require(bulletSpeed > 0.0, "Bullet speed must be positive")
  require(shootCooldown >= 0L, "Shoot cooldown cannot be negative")

final case class GameSettings(
                               arena: ArenaSettings = ArenaSettings(),
                               spaceship: SpaceshipSettings = SpaceshipSettings(),
                               shooting: ShootingSettings = ShootingSettings()
                             )

object GameSettings:
  val default: GameSettings = GameSettings()