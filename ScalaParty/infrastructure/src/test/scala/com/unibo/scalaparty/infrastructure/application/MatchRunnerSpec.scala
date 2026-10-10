package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.{IO, Ref}
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.ecs.GameEvent.MatchEnded
import com.unibo.scalaparty.core.engine.{GameEngine, TickResult}
import com.unibo.scalaparty.core.geometry.{Point2D, Shape}
import com.unibo.scalaparty.core.model.{GameCommand, GameEvent, MatchOutcome, MatchState}
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.ports.MatchEventPublisher
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class MatchRunnerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  class Fixture:
    val matchId: MatchId = MatchId.random()
    val playerId: PlayerId = PlayerId.random()
    val entityId: EntityId = EntityId.generate()
    var publishedStates: List[MatchState] = List.empty
    var capturedCommands: List[GameCommand] = List.empty

    /** An engine running out of time once it has been updated `ticks` times. */
    def engineEndingAfter(ticks: Int): GameEngine = new GameEngine:
      private var updates = 0

      override def arena: Shape.AABB = Shape.AABB(100.0, 100.0, Point2D.origin)

      override def update(commands: List[GameCommand], dt: Long): TickResult =
        updates += 1
        capturedCommands = capturedCommands ++ commands
        TickResult(List.empty, Option.when(updates >= ticks)(MatchEnded(MatchOutcome.TimeUp)).toSet)

    val publisher: MatchEventPublisher[IO] = new MatchEventPublisher[IO]:
      def broadcastState(mId: MatchId, state: MatchState): IO[Unit] = IO:
        publishedStates = publishedStates :+ state

      def broadcastEvent(mId: MatchId, event: GameEvent): IO[Unit] = IO.unit

    /** A runner whose match is over after exactly the given number of ticks, by default with no player ever leaving. */
    def runnerFor(
        session: MatchSession,
        ticks: Int,
        commands: GameCommandService,
        roster: Option[IO[Set[PlayerId]]] = None
    ): MatchRunner =
      val players = roster.getOrElse(IO.pure(session.players.keySet))
      new MatchRunner(session, commands, engineEndingAfter(ticks), publisher, players)

  "A MatchRunner".should:

    "execute ticks and publish state changes at each interval".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty)

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 3, commandService)
        _ <- runner.run
      yield f.publishedStates.length shouldEqual 3

    "number the published states by their tick".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty)

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 3, commandService)
        _ <- runner.run
      yield f.publishedStates.map(_.tick) shouldEqual List(0L, 1L, 2L)

    "end on its own once the engine reports the end of the match".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty)

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 2, commandService)
        // The timeout is what proves termination: an endless stream would never get here.
        _ <- runner.run.timeout(10.seconds)
      yield f.publishedStates.length shouldEqual 2

    "complete with the outcome reported by the engine".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty)

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 1, commandService)
        outcome <- runner.run
      yield outcome shouldBe MatchOutcome.TimeUp

    "drain and process queued player commands during execution".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 1, commandService)
        _ <- commandService.handleCommand(f.matchId, f.playerId, PlayerInput.Rotate(45.0))
        _ <- runner.run
      yield
        f.capturedCommands.length shouldEqual 1
        f.capturedCommands.head shouldEqual GameCommand.RotateCommand(f.entityId, 45.0)

    "ignore unmapped player commands not present in the session mapping".in:
      val f = Fixture()
      val unregisteredPlayer = PlayerId.random()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 1, commandService)
        _ <- commandService.handleCommand(f.matchId, unregisteredPlayer, PlayerInput.Shoot)
        _ <- runner.run
      yield f.capturedCommands shouldBe empty

    "tell the engine to remove the spaceship of a player who left, only once".in:
      val f = Fixture()
      val staying = PlayerId.random()
      val stayingEntity = EntityId.generate()
      val session =
        MatchSession(f.matchId, Map(f.playerId -> f.entityId, staying -> stayingEntity))

      for
        commandService <- GameCommandService()
        roster         <- Ref.of[IO, Set[PlayerId]](Set(staying))
        runner = f.runnerFor(session, ticks = 3, commandService, Some(roster.get))
        _ <- runner.run
      yield f.capturedCommands shouldBe List(GameCommand.LeaveCommand(f.entityId))

    "leave the spaceships of the players still in the match alone".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 3, commandService)
        _ <- runner.run
      yield f.capturedCommands shouldBe empty
