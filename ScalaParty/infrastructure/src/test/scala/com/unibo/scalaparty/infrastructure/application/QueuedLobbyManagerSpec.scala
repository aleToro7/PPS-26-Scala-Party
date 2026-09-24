package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{JoinOutcome, LeaveOutcome, MatchId, PlayerId}
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
        matches <- lobby.activeMatches
      yield matches shouldBe empty

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
        matches     <- lobby.activeMatches
      yield
        firstJoins shouldBe JoinOutcome.Queued(playersAhead = 0)
        matches.map(_.matchId) shouldBe Set(matchIdOf(secondJoins))
        matches.map(_.players) shouldBe Set(Set(first, second))

    "start a separate match for each player while rooms are free".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby       <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxMatches = 2)
        firstJoins  <- lobby.join(first)
        secondJoins <- lobby.join(second)
        matches     <- lobby.activeMatches
      yield
        matchIdOf(firstJoins) should not be matchIdOf(secondJoins)
        matches.map(_.players) shouldBe Set(Set(first), Set(second))

    "queue the players arriving once every room is taken".in:
      val players = List.fill(3)(PlayerId.random())
      for
        lobby    <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxMatches = 2)
        outcomes <- players.traverse(lobby.join)
        waiting  <- lobby.waitingPlayers
      yield
        outcomes.last shouldBe JoinOutcome.Queued(playersAhead = 0)
        waiting shouldBe Vector(players.last)

  "a bounded queue".should:
    "turn a player away once the queue is full".in:
      val players = List.fill(3)(PlayerId.random())
      for
        lobby    <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxQueued = 1)
        outcomes <- players.traverse(lobby.join)
        waiting  <- lobby.waitingPlayers
      yield
        outcomes.last shouldBe JoinOutcome.Rejected
        waiting shouldBe Vector(players(1))

    "admit nobody but the players when no one may wait".in:
      val playing = PlayerId.random()
      val latecomer = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxQueued = 0)
        _       <- lobby.join(playing)
        outcome <- lobby.join(latecomer)
        matches <- lobby.activeMatches
      yield
        outcome shouldBe JoinOutcome.Rejected
        matches.map(_.players) shouldBe Set(Set(playing))

    "still let in the player completing a match".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](minPlayers = 2, maxPlayers = 2, maxQueued = 1)
        _       <- lobby.join(first)
        outcome <- lobby.join(second)
      yield outcome match
        case JoinOutcome.Playing(activeMatch) => activeMatch.players shouldBe Set(first, second)
        case other => fail(s"expected the player to be playing, got $other")

    "keep reporting a player already waiting as queued".in:
      val playing = PlayerId.random()
      val waitingPlayer = PlayerId.random()
      for
        lobby     <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxQueued = 1)
        _         <- lobby.join(playing)
        _         <- lobby.join(waitingPlayer)
        joinAgain <- lobby.join(waitingPlayer)
      yield joinAgain shouldBe JoinOutcome.Queued(playersAhead = 0)

    "take a player in again once somebody has left the queue".in:
      val playing = PlayerId.random()
      val giveUp = PlayerId.random()
      val latecomer = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxQueued = 1)
        _       <- lobby.join(playing)
        _       <- lobby.join(giveUp)
        _       <- lobby.leave(giveUp)
        outcome <- lobby.join(latecomer)
      yield outcome shouldBe JoinOutcome.Queued(playersAhead = 0)

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
        matches <- lobby.activeMatches
      yield
        started shouldBe None
        matches shouldBe empty

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

    "be a no-op for a match that is not being played".in:
      val playerId = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        outcome <- lobby.join(playerId)
        started <- lobby.finishMatch(MatchId.random())
        matches <- lobby.activeMatches
      yield
        started shouldBe None
        matches.map(_.matchId) shouldBe Set(matchIdOf(outcome))

    "free only the room of the finished match".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      val waiting = PlayerId.random()
      for
        lobby       <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxMatches = 2)
        firstJoins  <- lobby.join(first)
        secondJoins <- lobby.join(second)
        _           <- lobby.join(waiting)
        started     <- lobby.finishMatch(matchIdOf(firstJoins))
        matches     <- lobby.activeMatches
      yield
        started.map(_.players) shouldBe Some(Set(waiting))
        matches.map(_.matchId) should contain(matchIdOf(secondJoins))
        matches.map(_.players) shouldBe Set(Set(second), Set(waiting))

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
        joins   <- lobby.join(playing)
        _       <- lobby.join(next)
        outcome <- lobby.leave(playing)
        waiting <- lobby.waitingPlayers
      yield
        outcome.disbanded shouldBe Some(matchIdOf(joins))
        outcome.started.map(_.players) shouldBe Some(Set(next))
        waiting shouldBe empty

    "disband the emptied match even when nobody is waiting to take the room".in:
      val playing = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO]()
        joins   <- lobby.join(playing)
        outcome <- lobby.leave(playing)
        matches <- lobby.activeMatches
      yield
        outcome shouldBe LeaveOutcome(disbanded = Some(matchIdOf(joins)), started = None)
        matches shouldBe empty

    "leave the other matches untouched when a match is disbanded".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby       <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxMatches = 2)
        _           <- lobby.join(first)
        secondJoins <- lobby.join(second)
        _           <- lobby.leave(first)
        matches     <- lobby.activeMatches
      yield matches.map(m => m.matchId -> m.players) shouldBe Set(matchIdOf(secondJoins) -> Set(second))

    "keep the match going while other players are still in it".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        lobby   <- QueuedLobbyManager.of[IO](minPlayers = 2)
        _       <- lobby.join(first)
        _       <- lobby.join(second)
        outcome <- lobby.leave(first)
        matches <- lobby.activeMatches
      yield
        outcome shouldBe LeaveOutcome(disbanded = None, started = None)
        matches.map(_.players) shouldBe Set(Set(second))

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
        outcome <- lobby.leave(PlayerId.random())
        matches <- lobby.activeMatches
      yield
        outcome shouldBe LeaveOutcome(disbanded = None, started = None)
        matches.map(_.players) shouldBe Set(Set(playerId))

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

  "the number of matches".should:
    "be rejected when no match is allowed to run".in:
      for result <- QueuedLobbyManager.of[IO](maxMatches = 0).attempt
      yield result.isLeft shouldBe true

  "the size of the queue".should:
    "be rejected when it cannot gather the players a match needs".in:
      for result <- QueuedLobbyManager.of[IO](minPlayers = 3, maxQueued = 1).attempt
      yield result.isLeft shouldBe true
