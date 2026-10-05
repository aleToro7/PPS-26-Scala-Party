package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.ecs.*
import com.unibo.scalaparty.core.ecs.GameEvent.Death
import com.unibo.scalaparty.core.geometry.{Point2D, Vector2D}
import com.unibo.scalaparty.core.model.{Effect, GameSettings, PowerUp, Stat, StatModifier}
import com.unibo.scalaparty.core.utils.PseudoRandom
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SystemPipelineSpec extends AnyFlatSpec with Matchers:

  private val defaultDt = 1_000L
  private val defaultSystems = SystemPipeline.default(GameSettings.default, players = 1).toList

  private def runDefaultPipeline(world: GameWorld): SystemOutput =
    defaultSystems.foldLeft((world, Set.empty[GameEvent])):
      case ((currentWorld, events), system) => system.update(currentWorld, events, defaultDt)

  "The default SystemPipeline" should "remove the destroyed entities after damage has been applied" in:
    defaultSystems should contain inOrder (DamageSystem, DeathSystem)

  it should "remove a spaceship in the same update that a lethal hit depletes its health" in:
    val shipId = EntityId.generate()
    val power = 10.0
    val world = GameWorld(
      List(
        EntityFactory.createSpaceship(Point2D.origin, Vector2D.zero, shipId, maxHealth = power),
        EntityFactory.createBullet(EntityId.generate(), Point2D(1.0, 0.0), Vector2D.zero, power = power)
      )
    )

    val (updatedWorld, events) = runDefaultPipeline(world)

    updatedWorld.entities shouldBe empty
    events should contain(Death(shipId))

  it should "hand out power-ups after entities are moved, before they are used to shoot" in:
    val movers = defaultSystems.takeWhile(!_.isInstanceOf[PowerUpSystem])
    val users = defaultSystems.dropWhile(!_.isInstanceOf[PowerUpSystem])

    movers should contain allOf (MovementSystem, CollisionSystem)
    users should contain(ShootingSystem)

  it should "let a spaceship shoot with a boost picked up in the same update" in:
    val shipId = EntityId.generate()
    val doubleDamage = PowerUp("damage", Effect.Boost(StatModifier(Stat.BulletPower, 2.0), 5_000L))
    val ship = EntityFactory.createSpaceship(Point2D.origin, Vector2D(1.0, 0.0), shipId)
    val spot = EntityFactory.createPowerUpSpot(Point2D.origin, List(doubleDamage), PseudoRandom(42L))
    val world = GameWorld(List(ship, spot)).updateComponent(shipId, ShootingComponent(isShooting = true))

    val (updatedWorld, _) = runDefaultPipeline(world)

    val bulletPowers = updatedWorld.findEntitiesWithComponent[BulletComponent].flatMap(_.components).collect:
      case BulletComponent(power, _) => power
    bulletPowers shouldBe List(2 * Weapon.default.bulletPower)

  it should "let the time of an update pass before anything else happens" in:
    defaultSystems.head shouldBe ClockSystem

  it should "judge the match only once every other system has run" in:
    defaultSystems.last shouldBe a[MatchEndSystem]
