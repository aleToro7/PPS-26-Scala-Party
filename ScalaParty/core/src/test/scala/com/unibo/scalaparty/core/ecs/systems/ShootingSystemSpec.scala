package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.Point2D
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ShootingSystemSpec extends AnyFlatSpec with Matchers:

  val positionComponent = PositionComponent(Point2D.origin)

  "ShootingSystem" should "not modify the world if there are no entities with shooting components" in:
    val entityId = EntityId.generate()
    val world = GameWorld(List((entityId, Nil)))
    val dt = 1_000L
    val (updatedWorld, events) = ShootingSystem.update(world, Set.empty, dt)
    events shouldBe empty
    updatedWorld shouldBe world

  "ShootingSystem" should "not modify the world if there are no entities that are shooting" in:
    val entityId = EntityId.generate()
    val component = ShootingComponent(bulletPower = 0, bulletSpeed = 0, shootCooldown = 0, isShooting = false)
    val world = GameWorld(List((entityId, List(component))))
    val dt = 1_000L
    val (updatedWorld, events) = ShootingSystem.update(world, Set.empty, dt)
    events shouldBe empty
    updatedWorld shouldBe world

  "ShootingSystem" should "decrease cooldown timer for shooting components" in:
    val cooldown = 10_000L
    val entityId = EntityId.generate()
    val component = ShootingComponent(
      bulletPower = 0,
      bulletSpeed = 0,
      shootCooldown = cooldown,
      isShooting = false,
      cooldownTimer = cooldown
    )
    val world = GameWorld(List((entityId, List(component))))
    val dt = 1_000L
    val (updatedWorld, _) = ShootingSystem.update(world, Set.empty, dt)
    val updatedShootingComponent = updatedWorld
      .findComponents(entityId)
      .getOrElse(Nil)
      .collectFirst { case sc: ShootingComponent => sc }
    updatedShootingComponent.value.cooldownTimer should be < cooldown

  "ShootingSystem" should "not decrease cooldown timer below zero" in:
    val entityId = EntityId.generate()
    val component =
      ShootingComponent(bulletPower = 0, bulletSpeed = 0, shootCooldown = 0, isShooting = false, cooldownTimer = 0)
    val world = GameWorld(List((entityId, List(component))))
    val dt = 1_000L
    val (updatedWorld, _) = ShootingSystem.update(world, Set.empty, dt)
    val updatedShootingComponent = updatedWorld
      .findComponents(entityId)
      .getOrElse(Nil)
      .collectFirst { case sc: ShootingComponent => sc }
    updatedShootingComponent.value.cooldownTimer shouldBe 0
    updatedWorld.id shouldBe world.id

  "ShootingSystem" should "add a bullet to the world if an entity is shooting" in:
    val entityId = EntityId.generate()
    val shootingComponent = ShootingComponent(bulletPower = 0, bulletSpeed = 0, shootCooldown = 0, isShooting = true)
    val entity = (entityId, List(shootingComponent, positionComponent))
    val world = GameWorld(List(entity))
    val dt = 1_000L
    val (updatedWorld, events) = ShootingSystem.update(world, Set.empty, dt)
    events shouldBe empty
    world.entities should have length 1
    updatedWorld.id should not be world.id
    updatedWorld.entities should have length 2
    val bullets = updatedWorld.findEntitiesWithComponent[BulletComponent]
    bullets should have length 1

  "ShootingSystem" should "reset shooting component after successfully shooting" in:
    val cooldown = 100L
    val entityId = EntityId.generate()
    val shootingComponent =
      ShootingComponent(bulletPower = 0, bulletSpeed = 0, shootCooldown = cooldown, isShooting = true)
    val entity = (entityId, List(shootingComponent, positionComponent))
    val world = GameWorld(List(entity))
    val dt = 1_000L
    val (updatedWorld, _) = ShootingSystem.update(world, Set.empty, dt)
    val updatedShootingComponent = updatedWorld
      .findComponents(entityId)
      .getOrElse(Nil)
      .collectFirst { case sc: ShootingComponent => sc }
    updatedShootingComponent.value.isShooting shouldBe false
    updatedShootingComponent.value.cooldownTimer shouldBe cooldown

  "ShootingSystem" should "not fire if cooldown time is not met" in:
    val cooldown = 10_000L
    val shootingComponent = ShootingComponent(
      bulletSpeed = 0,
      bulletPower = 0,
      shootCooldown = cooldown,
      isShooting = true,
      cooldownTimer = cooldown
    )
    val entityId = EntityId.generate()
    val entity = (entityId, List(shootingComponent, positionComponent))
    val world = GameWorld(List(entity))
    val dt = 1_000L
    val (updatedWorld, events) = ShootingSystem.update(world, Set.empty, dt)
    val updatedShootingComponent = updatedWorld
      .findComponents(entityId)
      .getOrElse(Nil)
      .collectFirst { case sc: ShootingComponent => sc }
    updatedShootingComponent.value.isShooting shouldBe true
    updatedShootingComponent.value.cooldownTimer should be < cooldown
