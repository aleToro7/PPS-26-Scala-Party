package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.dto.{EntityDto, PlayerCommand as DtoCommand}
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

class MatchRunnerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  class Fixture:
    val matchId: MatchId = MatchId.random()
    val playerId: PlayerId = PlayerId.random()
    val entityId: EntityId = EntityId.generate()

    var capturedCommands: List[DtoCommand] = List.empty
    var publishedStates: List[List[EntityDto]] = List.empty

    val engine: GameEngine = new GameEngine:
      def update(commands: List[DtoCommand], dt: Long): List[EntityDto] =
        capturedCommands = capturedCommands ++ commands
        List.empty

    val publisher: MatchEventPublisher[IO] = new MatchEventPublisher[IO]:
      def publishState(mId: MatchId, entities: List[EntityDto]): IO[Unit] = IO:
        publishedStates = publishedStates :+ entities

  "A MatchRunner" should:

    "execute ticks and publish state changes at each interval" in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map.empty, GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = new MatchRunner(session, commandService, f.engine, f.publisher)
        _ <- runner.run.take(3).compile.drain
      yield
        f.publishedStates.length shouldEqual 3

    "drain and process queued player commands during execution" in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = new MatchRunner(session, commandService, f.engine, f.publisher)
        _ <- commandService.handleCommand(f.matchId, f.playerId, IntentCommand.Rotate(45.0))
        _ <- runner.run.take(1).compile.drain
      yield
        f.capturedCommands.length shouldEqual 1
        f.capturedCommands.head shouldEqual DtoCommand.RotateCommand(f.entityId, 45.0)

    "ignore unmapped player commands not present in the session mapping" in:
      val f = Fixture()
      val unregisteredPlayer = PlayerId.random()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = new MatchRunner(session, commandService, f.engine, f.publisher)
        _ <- commandService.handleCommand(f.matchId, unregisteredPlayer, IntentCommand.Shoot)
        _ <- runner.run.take(1).compile.drain
      yield
        f.capturedCommands shouldBe empty