package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.model.GameCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class MatchRunnerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  class Fixture:
    val matchId: MatchId = MatchId.random()
    val playerId: PlayerId = PlayerId.random()
    val entityId: EntityId = EntityId.generate()

    var capturedCommands: List[GameCommand] = List.empty
    var publishedStates: List[List[EntityDto]] = List.empty

    val engine: GameEngine = (commands: List[GameCommand], dt: Long) =>
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
      yield f.publishedStates.length shouldEqual 3

    "drain and process queued player commands during execution" in:
      val f = Fixture()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = new MatchRunner(session, commandService, f.engine, f.publisher)
        _ <- commandService.handleCommand(f.matchId, f.playerId, PlayerInput.Rotate(45.0))
        _ <- runner.run.take(1).compile.drain
      yield
        f.capturedCommands.length shouldEqual 1
        f.capturedCommands.head shouldEqual GameCommand.RotateCommand(f.entityId, 45.0)

    "ignore unmapped player commands not present in the session mapping" in:
      val f = Fixture()
      val unregisteredPlayer = PlayerId.random()
      val session = MatchSession(f.matchId, Map(f.playerId -> f.entityId), GameWorld(Map.empty))

      for
        commandService <- GameCommandService()
        runner = new MatchRunner(session, commandService, f.engine, f.publisher)
        _ <- commandService.handleCommand(f.matchId, unregisteredPlayer, PlayerInput.Shoot)
        _ <- runner.run.take(1).compile.drain
      yield f.capturedCommands shouldBe empty
