package com.unibo.scalaparty.core.dto

import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}

/** ADT for all serializable entity Data Transfer Objects (DTOs).
 */
enum EntityDto:

  /** Represents a player spaceship.
   *
   *  @param id the unique identifier of the spaceship entity
   *  @param position the current 2D spatial coordinates of the spaceship
   *  @param velocity the current movement vector representing the speed and direction of the spaceship
   */
  case Spaceship(id: EntityId, position: Point2D, velocity: Vector2D)
