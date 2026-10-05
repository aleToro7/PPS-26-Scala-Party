package com.unibo.scalaparty.core.ecs

import com.unibo.scalaparty.core.geometry.{Point2D, Shape, Vector2D}
import com.unibo.scalaparty.core.model.{Effect, GameSettings, PowerUp, ShootingSettings, SpaceshipSettings}
import com.unibo.scalaparty.core.utils.{collectFirstOfClass, PseudoRandom}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.{contain, should, shouldBe}

class EntityFactorySpec extends AnyFlatSpec:

  "A Spaceship" should "be created with the correct components" in:
    val position = Point2D(10, 20)
    val velocity = Vector2D(1, 1)
    val (entityId, components) = EntityFactory.createSpaceship(position, velocity)
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Spaceship)
    )

  it should "be created at full health and able to deal collision damage" in:
    val maxHealth = 80.0
    val collisionDamage = 15.0
    val (_, components) = EntityFactory.createSpaceship(
      position = Point2D.origin,
      velocity = Vector2D.zero,
      maxHealth = maxHealth,
      collisionDamage = collisionDamage
    )
    components should contain allOf (
      HealthComponent.full(maxHealth),
      CollisionDamageComponent(collisionDamage)
    )

  it should "be created from settings with the correct health component" in:
    val health = 80.0
    val settings = GameSettings.default.copy(
      spaceship = SpaceshipSettings(
        maxHealth = health,
      ),
    )
    val position = Point2D(5, 5)
    val direction = Vector2D(1, 0)
    val (_, components) = EntityFactory.createSpaceshipFromConfig(settings)(position, direction)
    val healthComponent = components.collectFirstOfClass[HealthComponent]
    healthComponent.value.current shouldBe health
    healthComponent.value.max shouldBe health

  it should "be created from settings with the correct collision-damage component" in:
    val collisionDamage = 15.0
    val settings = GameSettings.default.copy(
      spaceship = SpaceshipSettings(
        collisionDamage = collisionDamage,
      ),
    )
    val position = Point2D(5, 5)
    val direction = Vector2D(1, 0)
    val (_, components) = EntityFactory.createSpaceshipFromConfig(settings)(position, direction)
    val collisionComponent = components.collectFirstOfClass[CollisionDamageComponent]
    collisionComponent.value.damage shouldBe collisionDamage

  it should "should be created from settings with the correct shooting component" in:
    val bulletPower = 10.0
    val bulletSpeed = 20.0
    val shootCooldown = 100
    val muzzleOffset = 1.0
    val settings = GameSettings.default.copy(
      shooting = ShootingSettings(
        bulletPower = bulletPower,
        bulletSpeed = bulletSpeed,
        shootCooldown = shootCooldown,
        muzzleOffset = muzzleOffset
      )
    )
    val position = Point2D(5, 5)
    val direction = Vector2D(1, 0)
    val (_, components) = EntityFactory.createSpaceshipFromConfig(settings)(position, direction)
    val shootingComponent = components.collectFirstOfClass[ShootingComponent]
    shootingComponent.value.weapon.bulletPower shouldBe bulletPower
    shootingComponent.value.weapon.bulletSpeed shouldBe bulletSpeed
    shootingComponent.value.weapon.shootCooldown shouldBe shootCooldown
    shootingComponent.value.weapon.muzzleOffset shouldBe muzzleOffset

  "A Bullet" should "be created with the correct components" in:
    val shooterId = EntityId.generate()
    val position = Point2D(15, 25)
    val velocity = Vector2D(2, 0)
    val power = 12.5
    val bulletId = EntityId.generate()
    val (createdBulletId, components) = EntityFactory.createBullet(shooterId, position, velocity, power, bulletId)
    createdBulletId shouldBe bulletId
    components should contain allOf (
      PositionComponent(position),
      MovementComponent(velocity),
      EntityTypeComponent(EntityType.Bullet),
      BulletComponent(power, shooterId)
    )

  "A Wall" should "be created with the correct components" in:
    val position = Point2D(5, 5)
    val width = 10.0
    val height = 2.0
    val (wallId, components) =
      EntityFactory.createWall(position, width, height)
    components should contain allOf (
      PositionComponent(position),
      ShapeComponent(Shape.AABB(width, height, Point2D.origin)),
      EntityTypeComponent(EntityType.Wall)
    )

  "A PowerUpSpot" should "be created at its position, holding a power-up from the catalog" in:
    val position = Point2D(5, 5)
    val catalog = List(PowerUp("repair", Effect.Repair(30.0)))
    val random = PseudoRandom(42L)
    val (_, components) = EntityFactory.createPowerUpSpot(position, catalog, random)
    components should contain allOf (
      PositionComponent(position),
      PowerUpSpotComponent.stocked(catalog, random)
    )

  it should "have no shape, so that moving entities pass through it" in:
    val (_, components) =
      EntityFactory.createPowerUpSpot(Point2D.origin, List(PowerUp("repair", Effect.Repair(30.0))), PseudoRandom(42L))
    components.collectFirstOfClass[ShapeComponent] shouldBe None
