package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ShootingSystemSpec extends AnyFlatSpec with Matchers with OptionValues:

  private val entityId = EntityId.generate()
  private val defaultDt = 1_000L
  private val positionComponent = PositionComponent(Point2D.origin)
  private val movementComponent = MovementComponent(Vector2D(1.0, 0.0))
  private val shipBaseComponents = List(positionComponent, movementComponent)

  private def weaponWith(
      shootCooldown: Long,
      bulletPower: Double = 10.0,
      bulletSpeed: Double = 100.0,
      muzzleOffset: Double = 0.0
  ): Weapon =
    Weapon(bulletPower, bulletSpeed, shootCooldown, muzzleOffset)

  private def worldWith(components: Component*): GameWorld =
    GameWorld(List((entityId, components.toList)))

  private def shipWorldWith(shooting: ShootingComponent): GameWorld =
    GameWorld(List((entityId, shooting :: shipBaseComponents)))

  private def updateWorld(world: GameWorld, dt: Long = defaultDt): (GameWorld, Set[GameEvent]) =
    ShootingSystem.update(world, Set.empty, dt)

  extension (world: GameWorld)
    private def shootingComponent: ShootingComponent =
      world
        .findComponents(entityId)
        .flatMap(_.collectFirst { case sc: ShootingComponent => sc })
        .value

  "ShootingSystem" should "not modify the world if there are no entities with shooting components" in:
    val world = worldWith()
    val (updatedWorld, events) = updateWorld(world)

    events shouldBe empty
    updatedWorld shouldBe world

  it should "not modify the world if there are no entities that are shooting" in:
    val world = worldWith(ShootingComponent(Weapon.default))
    val (updatedWorld, events) = updateWorld(world)

    events shouldBe empty
    updatedWorld shouldBe world

  it should "decrease cooldown timer for shooting components" in:
    val cooldown = 10_000L
    val world = worldWith(ShootingComponent(weaponWith(cooldown), isShooting = false, cooldownTimer = cooldown))

    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.shootingComponent.cooldownTimer should be < cooldown

  it should "not decrease cooldown timer below zero" in:
    val world = worldWith(ShootingComponent(Weapon.default))

    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.shootingComponent.cooldownTimer shouldBe 0L
    updatedWorld.id shouldBe world.id

  it should "add a bullet to the world if an entity is shooting" in:
    val world = shipWorldWith(ShootingComponent(weapon = Weapon.default, isShooting = true))

    val (updatedWorld, events) = updateWorld(world)

    events shouldBe empty
    world.entities should have length 1
    updatedWorld.id should not be world.id
    updatedWorld.entities should have length 2
    updatedWorld.findEntitiesWithComponent[BulletComponent] should have length 1

  it should "reset shooting component after successfully shooting" in:
    val cooldown = 100L
    val world = shipWorldWith(ShootingComponent(weapon = weaponWith(cooldown), isShooting = true))

    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.shootingComponent.isShooting shouldBe false
    updatedWorld.shootingComponent.cooldownTimer shouldBe cooldown

  it should "ignore the shoot intent if cooldown time is not met" in:
    val cooldown = 10_000L
    val world = shipWorldWith(ShootingComponent(weaponWith(cooldown), isShooting = true, cooldownTimer = cooldown))

    val (updatedWorld, _) = updateWorld(world)

    updatedWorld.findEntitiesWithComponent[BulletComponent] shouldBe empty
    updatedWorld.shootingComponent.isShooting shouldBe false
    updatedWorld.shootingComponent.cooldownTimer should be < cooldown

  it should "spawn the bullet in front of the shooter at the muzzle offset" in:
    val weapon = weaponWith(shootCooldown = 500L, muzzleOffset = 12.0)
    val world = shipWorldWith(ShootingComponent(weapon, isShooting = true))

    val (updatedWorld, _) = updateWorld(world)

    val (_, bulletComponents) = updatedWorld.findEntitiesWithComponent[BulletComponent].head
    val bulletPosition = bulletComponents.collectFirst { case pc: PositionComponent => pc.position }.value
    bulletPosition shouldBe Point2D(12.0, 0.0)

  it should "spawn bullet with correct velocity, power and shooterId" in:
    val weapon = weaponWith(shootCooldown = 500L, bulletPower = 25.0, bulletSpeed = 150.0)
    val movingRight = MovementComponent(Vector2D(5.0, 0.0))
    val world = worldWith(ShootingComponent(weapon, isShooting = true), positionComponent, movingRight)

    val (updatedWorld, _) = updateWorld(world, dt = 16L)

    val bullets = updatedWorld.findEntitiesWithComponent[BulletComponent]
    bullets should have length 1

    val (bulletId, bulletComponents) = bullets.head
    bulletId should not be entityId

    val bulletComp = bulletComponents.collectFirst { case bc: BulletComponent => bc }.value
    bulletComp.power shouldBe weapon.bulletPower
    bulletComp.shooterId shouldBe entityId

    val bulletMovement = bulletComponents.collectFirst { case mc: MovementComponent => mc }.value
    bulletMovement.velocity shouldBe Vector2D(weapon.bulletSpeed, 0.0)
