package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class LobbyManagerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers:

  "a freshly created LobbyManager" - {
    "has no active matches" in {
      for
        lobby <- LobbyManager.of[IO]
        ids   <- lobby.activeMatchIds
      yield ids shouldBe empty
    }

    "has no pending match" in {
      for
        lobby   <- LobbyManager.of[IO]
        pending <- lobby.pendingMatch
      yield pending shouldBe None
    }
  }

  "registerMatch" - {
    "adds an empty match to the active matches" in {
      val matchId = MatchId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.registerMatch(matchId)
        ids     <- lobby.activeMatchIds
        players <- lobby.playersInMatch(matchId)
      yield
        ids shouldBe Set(matchId)
        players shouldBe empty
    }
  }

  "removeMatch" - {
    "removes a match and clears it if it was the pending one" in {
      val matchId = MatchId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.registerMatch(matchId)
        _       <- lobby.setPendingMatch(Some(matchId))
        _       <- lobby.removeMatch(matchId)
        ids     <- lobby.activeMatchIds
        pending <- lobby.pendingMatch
      yield
        ids shouldBe empty
        pending shouldBe None
    }
  }

  "addPlayerToMatch" - {
    "adds a player to an existing match" in {
      val matchId  = MatchId.random()
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.registerMatch(matchId)
        _       <- lobby.addPlayerToMatch(matchId, playerId)
        players <- lobby.playersInMatch(matchId)
      yield players shouldBe Set(playerId)
    }

    "creates the match entry if it doesn't exist yet" in {
      val matchId  = MatchId.random()
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.addPlayerToMatch(matchId, playerId)
        players <- lobby.playersInMatch(matchId)
      yield players shouldBe Set(playerId)
    }
  }

  "removePlayerFromMatch" - {
    "removes a player from a match, keeping the others" in {
      val matchId   = MatchId.random()
      val playerOne = PlayerId.random()
      val playerTwo = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.registerMatch(matchId)
        _       <- lobby.addPlayerToMatch(matchId, playerOne)
        _       <- lobby.addPlayerToMatch(matchId, playerTwo)
        _       <- lobby.removePlayerFromMatch(matchId, playerOne)
        players <- lobby.playersInMatch(matchId)
      yield players shouldBe Set(playerTwo)
    }
  }

  "joinLobby" - {
    "opens a new match for the first player" in {
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        players <- lobby.playersInMatch(matchId)
        pending <- lobby.pendingMatch
      yield
        players shouldBe Set(playerId)
        pending shouldBe Some(matchId)
    }

    "assigns further players to the same pending match" in {
      val playerOne = PlayerId.random()
      val playerTwo = PlayerId.random()
      for
        lobby    <- LobbyManager.of[IO]
        matchOne <- lobby.joinLobby(playerOne)
        matchTwo <- lobby.joinLobby(playerTwo)
        players  <- lobby.playersInMatch(matchOne)
      yield
        matchTwo shouldBe matchOne
        players shouldBe Set(playerOne, playerTwo)
    }

    "stops being pending once it reaches the max number of players" in {
      val players = List.fill(LobbyManager.MaxPlayersPerMatch)(PlayerId.random())
      for
        lobby      <- LobbyManager.of[IO]
        matchIds   <- players.traverse(lobby.joinLobby)
        pending    <- lobby.pendingMatch
        inMatch    <- lobby.playersInMatch(matchIds.head)
      yield
        matchIds.toSet shouldBe Set(matchIds.head)
        inMatch shouldBe players.toSet
        pending shouldBe None
    }

    "opens a new match once the pending one is full" in {
      val firstBatch = List.fill(LobbyManager.MaxPlayersPerMatch)(PlayerId.random())
      val extraPlayer = PlayerId.random()
      for
        lobby        <- LobbyManager.of[IO]
        fullMatchIds <- firstBatch.traverse(lobby.joinLobby)
        newMatchId   <- lobby.joinLobby(extraPlayer)
        pending      <- lobby.pendingMatch
      yield
        newMatchId should not be fullMatchIds.head
        pending shouldBe Some(newMatchId)
    }
  }

  "leaveLobby" - {
    "removes the player but keeps the match if others remain" in {
      val matchId   = MatchId.random()
      val playerOne = PlayerId.random()
      val playerTwo = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        _       <- lobby.registerMatch(matchId)
        _       <- lobby.addPlayerToMatch(matchId, playerOne)
        _       <- lobby.addPlayerToMatch(matchId, playerTwo)
        _       <- lobby.leaveLobby(matchId, playerOne)
        ids     <- lobby.activeMatchIds
        players <- lobby.playersInMatch(matchId)
      yield
        ids shouldBe Set(matchId)
        players shouldBe Set(playerTwo)
    }

    "removes the match entirely once the last player leaves" in {
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        _       <- lobby.leaveLobby(matchId, playerId)
        ids     <- lobby.activeMatchIds
      yield ids shouldBe empty
    }

    "clears the pending match if the last player leaves it" in {
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        _       <- lobby.leaveLobby(matchId, playerId)
        pending <- lobby.pendingMatch
      yield pending shouldBe None
    }

    "is a no-op if the match doesn't exist" in {
      val matchId  = MatchId.random()
      val playerId = PlayerId.random()
      for
        lobby <- LobbyManager.of[IO]
        _     <- lobby.leaveLobby(matchId, playerId)
        ids   <- lobby.activeMatchIds
      yield ids shouldBe empty
    }
  }
