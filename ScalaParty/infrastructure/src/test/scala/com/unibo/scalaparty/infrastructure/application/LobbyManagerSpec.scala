package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class LobbyManagerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  "a freshly created LobbyManager".should:
    "have no active matches".in:
      for
        lobby <- LobbyManager.of[IO]
        ids   <- lobby.activeMatchIds
      yield ids shouldBe empty

    "have no pending match".in:
      for
        lobby   <- LobbyManager.of[IO]
        pending <- lobby.pendingMatch
      yield pending shouldBe None

  "joinLobby".should:
    "open a new match for the first player".in:
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        players <- lobby.playersInMatch(matchId)
        pending <- lobby.pendingMatch
      yield
        players shouldBe Set(playerId)
        pending shouldBe Some(matchId)

    "assign further players to the same pending match".in:
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

    "stop being pending once it reaches the max number of players".in:
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

    "open a new match once the pending one is full".in:
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

  "leaveLobby".should:
    "remove the player but keep the match if others remain".in:
      val playerOne = PlayerId.random()
      val playerTwo = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerOne)
        _       <- lobby.joinLobby(playerTwo)
        _       <- lobby.leaveLobby(matchId, playerOne)
        ids     <- lobby.activeMatchIds
        players <- lobby.playersInMatch(matchId)
      yield
        ids shouldBe Set(matchId)
        players shouldBe Set(playerTwo)

    "remove the match entirely once the last player leaves".in:
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        _       <- lobby.leaveLobby(matchId, playerId)
        ids     <- lobby.activeMatchIds
      yield ids shouldBe empty

    "clear the pending match if the last player leaves it".in:
      val playerId = PlayerId.random()
      for
        lobby   <- LobbyManager.of[IO]
        matchId <- lobby.joinLobby(playerId)
        _       <- lobby.leaveLobby(matchId, playerId)
        pending <- lobby.pendingMatch
      yield pending shouldBe None

    "be a no-op if the match doesn't exist".in:
      val matchId  = MatchId.random()
      val playerId = PlayerId.random()
      for
        lobby <- LobbyManager.of[IO]
        _     <- lobby.leaveLobby(matchId, playerId)
        ids   <- lobby.activeMatchIds
      yield ids shouldBe empty