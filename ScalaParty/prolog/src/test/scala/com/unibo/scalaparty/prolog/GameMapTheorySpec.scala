package com.unibo.scalaparty.prolog

import alice.tuprolog.Term
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GameMapTheorySpec extends AnyWordSpec with Matchers:

  private val theory = PrologEngine.fromResource("theories/game_map.pl")
  private val supportedPlayers = 1 to 4

  private def solutions(goal: String): LazyList[Term] = theory.solve(Term.createTerm(goal))
  private def holds(goal: String): Boolean = solutions(goal).nonEmpty

  "The game map theory" should:

    "generate exactly one map for each supported number of players" in:
      supportedPlayers.foreach: players =>
        solutions(s"game_map($players, _)") should have size 1

    "generate one spawn per player" in:
      supportedPlayers.foreach: players =>
        holds(s"game_map($players, game_map(_, Spawns)), length(Spawns, $players)") shouldBe true

    "place every spawn within the arena" in:
      supportedPlayers.foreach: players =>
        holds(
          s"""game_map($players, game_map(arena(Width, Height), Spawns)),
             |forall(member(spawn(X, Y, _), Spawns), (X >= 0, X =< Width, Y >= 0, Y =< Height))""".stripMargin
        ) shouldBe true

    "place two players on opposite corners, facing each other" in:
      holds("game_map(2, game_map(arena(800, 800), [spawn(202, 202, 45.0), spawn(598, 598, 225.0)]))") shouldBe true

    "place four players on the four corners, all facing the center" in:
      holds(
        """game_map(4, game_map(arena(800, 800), [
          |  spawn(202, 202, 45.0), spawn(598, 202, 135.0), spawn(598, 598, 225.0), spawn(202, 598, 315.0)
          |]))""".stripMargin
      ) shouldBe true

    "generate no map for an unsupported number of players" in:
      List("0", "5", "-1", "2.0", "two").foreach: players =>
        holds(s"game_map($players, _)") shouldBe false
