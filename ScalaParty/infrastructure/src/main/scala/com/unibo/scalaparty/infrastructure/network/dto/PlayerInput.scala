package com.unibo.scalaparty.infrastructure.network.dto

enum PlayerInput:
  case Rotate(angle: Double)
  case Shoot
