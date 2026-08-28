package com.unibo.scalaparty.core.model

enum PlayerCommand:
  case Rotate(angle: Double)
  case Shoot
