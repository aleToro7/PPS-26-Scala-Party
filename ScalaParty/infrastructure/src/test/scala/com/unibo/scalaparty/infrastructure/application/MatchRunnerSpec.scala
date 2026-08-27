package com.unibo.scalaparty.infrastructure.application

import cats.effect.{IO, Ref}
import cats.effect.unsafe.implicits.global
import com.unibo.scalaparty.core.engine.GameEngine
import com.unibo.scalaparty.core.dto.{EntityDto, PlayerCommand as DtoCommand}
import com.unibo.scalaparty.core.model.PlayerCommand as IntentCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.ecs.{EntityId, GameWorld}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.*

class MatchRunnerSpec extends AnyFlatSpec with Matchers:

  "A MatchRunner" should "drain commands, execute engine updates, and publish state changes correctly" in {

    val matchId = MatchId.random()
    val playerId = PlayerId.random()
    val entityId = EntityId.generate()

    // Construct the MatchSession required by the updated MatchRunner
    val session = MatchSession(
      matchId = matchId,
      players = Map(playerId -> entityId),
      world = GameWorld(Map.empty)
    )

    val testProgram = for
      commandService <- GameCommandService()

      // Mock GameEngine implementation for testing purposes
      engine = new GameEngine:
        def update(commands: List[DtoCommand], dt: Long): List[EntityDto] =
          List.empty

      // Ref to capture published states across ticks
      publishedStatesRef <- Ref.of[IO, List[List[EntityDto]]](List.empty)

      publisher = new MatchEventPublisher[IO]:
        def publishState(mId: MatchId, entities: List[EntityDto]): IO[Unit] =
          publishedStatesRef.update(_ :+ entities)

      // Inject the MatchSession instead of the raw MatchId
      runner = new MatchRunner(session, commandService, engine, publisher)

      // Enqueue a sample player command using the mapped playerId
      _ <- commandService.handleCommand(matchId, playerId, IntentCommand.Rotate(90.0))

      // Execute the FS2 stream for exactly 2 ticks, then compile and drain
      _ <- runner.run.take(2).compile.drain

      states <- publishedStatesRef.get
    yield states

    val results = testProgram.unsafeRunSync()

    // Verify that the match runner executed two ticks and published state twice
    results.length shouldBe 2
  }