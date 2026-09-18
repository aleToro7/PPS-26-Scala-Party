package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.model.{GameCommand, GameEvent, MatchState}
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

    var capturedCommands: List[GameCommand] = List.empty
    var publishedStates: List[MatchState] = List.empty

    val engine: GameEngine = (commands: List[GameCommand], dt: Long) =>
      capturedCommands = capturedCommands ++ commands
      List.empty

    val publisher: MatchEventPublisher[IO] = new MatchEventPublisher[IO]:
      def broadcastState(mId: MatchId, state: MatchState): IO[Unit] = IO:
        publishedStates = publishedStates :+ state

      def broadcastEvent(mId: MatchId, event: GameEvent): IO[Unit] = IO.unit

    /** A runner ticking for exactly the given number of frames. */
    def runnerFor(session: MatchSession, ticks: Int, commands: GameCommandService): MatchRunner =
      new MatchRunner(session, commands, engine, publisher, MatchRunner.TickInterval * ticks.toLong)

  "A MatchRunner".should:

    "execute ticks and publish state changes at each interval".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty, GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 3, commandService)
        _ <- runner.run.compile.drain
      yield f.publishedStates.length shouldEqual 3

    "number the published states by their tick".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty, GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 3, commandService)
        _ <- runner.run.compile.drain
      yield f.publishedStates.map(_.tick) shouldEqual List(0L, 1L, 2L)

    "end on its own once the match duration has elapsed".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty, GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 2, commandService)
        // The timeout is what proves termination: an endless stream would never get here.
        _ <- runner.run.compile.drain.timeout(10.seconds)
      yield f.publishedStates.length shouldEqual 2

    "drain and process queued player commands during execution".in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 1, commandService)
        _ <- commandService.handleCommand(f.matchId, f.playerId, PlayerInput.Rotate(45.0))
        _ <- runner.run.compile.drain
      yield
        f.capturedCommands.length shouldEqual 1
        f.capturedCommands.head shouldEqual GameCommand.RotateCommand(f.entityId, 45.0)

    "ignore unmapped player commands not present in the session mapping".in:
      val f = Fixture()
      val unregisteredPlayer = PlayerId.random()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = f.runnerFor(session, ticks = 1, commandService)
        _ <- commandService.handleCommand(f.matchId, unregisteredPlayer, PlayerInput.Shoot)
        _ <- runner.run.compile.drain
      yield f.capturedCommands shouldBe empty
