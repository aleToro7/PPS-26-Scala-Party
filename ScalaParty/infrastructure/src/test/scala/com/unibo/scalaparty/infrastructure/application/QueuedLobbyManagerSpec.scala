package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{JoinOutcome, MatchId, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class QueuedLobbyManagerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  /** Extracts the identifier of the match a player has just been admitted to. */
  private def matchIdOf(outcome: JoinOutcome): MatchId = outcome match
    case JoinOutcome.Playing(activeMatch) => activeMatch.matchId
    case other => fail(s"expected the player to be playing, got $other")

  "a freshly created QueuedLobbyManager".should:
    "have no match being played".in:
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        current <- lobby.currentMatch
      yield current shouldBe None

    "have nobody waiting".in:
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        waiting <- lobby.waitingPlayers
      yield waiting shouldBe empty

  "join".should:
    "start a match right away for the first player".in:
      val playerId = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playerId)
      yield outcome match
        case JoinOutcome.Playing(activeMatch) => activeMatch.players shouldBe Set(playerId)
        case other => fail(s"expected the player to be playing, got $other")

    "queue the players arriving while a match is being played".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      val third = PlayerId.random()
      for
        lobby       <- QueuedLobbyManager.of[IO]()
        _           <- lobby.join(first)
        secondJoins <- lobby.join(second)
        thirdJoins  <- lobby.join(third)
        waiting     <- lobby.waitingPlayers
      yield
        secondJoins shouldBe JoinOutcome.Queued(playersAhead = 0)
        thirdJoins shouldBe JoinOutcome.Queued(playersAhead = 1)
        waiting shouldBe Vector(second, third)

    "never queue the same player twice".in:
      val playing = PlayerId.random()
      val waitingPlayer = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        _       <- lobby.join(playing)
        _       <- lobby.join(waitingPlayer)
        _       <- lobby.join(waitingPlayer)
        waiting <- lobby.waitingPlayers
      yield waiting shouldBe Vector(waitingPlayer)

    "keep reporting a player already in the match as playing".in:
      val playerId = PlayerId.random()
      for
        lobby     <- QueuedLobbyManager.of[IO]()
        firstJoin <- lobby.join(playerId)
        joinAgain <- lobby.join(playerId)
      yield joinAgain shouldBe firstJoin

    "hold the first players back until the minimum is reached".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby       <- QueuedLobbyManager.of[IO](minPlayers = 2)
        firstJoins  <- lobby.join(first)
        secondJoins <- lobby.join(second)
        current     <- lobby.currentMatch
      yield
        firstJoins shouldBe JoinOutcome.Queued(playersAhead = 0)
        current.map(_.matchId) shouldBe Some(matchIdOf(secondJoins))
        current.map(_.players) shouldBe Some(Set(first, second))

  "finishMatch".should:
    "hand the arena to the players left waiting".in:
      val playing = PlayerId.random()
      val queued = List.fill(3)(PlayerId.random())
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playing)
        _       <- queued.traverse(lobby.join)
        started <- lobby.finishMatch(matchIdOf(outcome))
        waiting <- lobby.waitingPlayers
      yield
        started.map(_.players) shouldBe Some(queued.toSet)
        waiting shouldBe empty

    "take no more players than a match can host".in:
      val playing = PlayerId.random()
      val queued = List.fill(QueuedLobbyManager.MaxPlayersPerMatch + 2)(PlayerId.random())
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playing)
        _       <- queued.traverse(lobby.join)
        started <- lobby.finishMatch(matchIdOf(outcome))
        waiting <- lobby.waitingPlayers
      yield
        started.map(_.players) shouldBe Some(queued.take(QueuedLobbyManager.MaxPlayersPerMatch).toSet)
        waiting shouldBe queued.drop(QueuedLobbyManager.MaxPlayersPerMatch).toVector

    "leave the arena free when nobody is waiting".in:
      val playerId = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playerId)
        started <- lobby.finishMatch(matchIdOf(outcome))
        current <- lobby.currentMatch
      yield
        started shouldBe None
        current shouldBe None

    "wait for the minimum number of players before starting the next match".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      val latecomer = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](minPlayers = 2)
        _       <- lobby.join(first)
        outcome <- lobby.join(second)
        _       <- lobby.join(latecomer)
        started <- lobby.finishMatch(matchIdOf(outcome))
        waiting <- lobby.waitingPlayers
      yield
        started shouldBe None
        waiting shouldBe Vector(latecomer)

    "be a no-op for a match that is not the one being played".in:
      val playerId = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playerId)
        started <- lobby.finishMatch(MatchId.random())
        current <- lobby.currentMatch
      yield
        started shouldBe None
        current.map(_.matchId) shouldBe Some(matchIdOf(outcome))

  "leave".should:
    "drop a waiting player and move the others up".in:
      val playing = PlayerId.random()
      val second = PlayerId.random()
      val third = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        _       <- lobby.join(playing)
        _       <- lobby.join(second)
        _       <- lobby.join(third)
        _       <- lobby.leave(second)
        ahead   <- lobby.playersAhead(third)
        waiting <- lobby.waitingPlayers
      yield
        ahead shouldBe Some(0)
        waiting shouldBe Vector(third)

    "hand the arena over when the last player of a match leaves".in:
      val playing = PlayerId.random()
      val next = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        _       <- lobby.join(playing)
        _       <- lobby.join(next)
        started <- lobby.leave(playing)
        waiting <- lobby.waitingPlayers
      yield
        started.map(_.players) shouldBe Some(Set(next))
        waiting shouldBe empty

    "keep the match going while other players are still in it".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](minPlayers = 2)
        _       <- lobby.join(first)
        _       <- lobby.join(second)
        started <- lobby.leave(first)
        current <- lobby.currentMatch
      yield
        started shouldBe None
        current.map(_.players) shouldBe Some(Set(second))

    "make a dropped player unreachable by the next match".in:
      val playing = PlayerId.random()
      val giveUp = PlayerId.random()
      val next = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playing)
        _       <- lobby.join(giveUp)
        _       <- lobby.join(next)
        _       <- lobby.leave(giveUp)
        started <- lobby.finishMatch(matchIdOf(outcome))
      yield started.map(_.players) shouldBe Some(Set(next))

    "be a no-op for a player nobody knows about".in:
      val playerId = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        _       <- lobby.join(playerId)
        started <- lobby.leave(PlayerId.random())
        current <- lobby.currentMatch
      yield
        started shouldBe None
        current.map(_.players) shouldBe Some(Set(playerId))

  "the match size".should:
    "be rejected when the minimum exceeds the maximum".in:
      for result <- QueuedLobbyManager.of[IO](minPlayers = 3, maxPlayers = 2).attempt
      yield result.isLeft shouldBe true

    "be rejected when fewer than one player is required".in:
      for result <- QueuedLobbyManager.of[IO](minPlayers = 0).attempt
      yield result.isLeft shouldBe true

    "be rejected when more players than a match can host are allowed".in:
      for result <- QueuedLobbyManager.of[IO](maxPlayers = QueuedLobbyManager.MaxPlayersPerMatch + 1).attempt
      yield result.isLeft shouldBe true
