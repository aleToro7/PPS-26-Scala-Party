package com.unibo.scalaparty.infrastructure.application

import cats.effect.{Ref, Sync}
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.AccessPort

private final case class LobbyState(
    activeMatches: Map[MatchId, Set[PlayerId]],
    pendingLobbyMatch: Option[MatchId]
)

private object LobbyState:
  val empty: LobbyState = LobbyState(Map.empty, None)

/** Core application service managing the logical state of the matchmaking lobby.
 *  Groups incoming players into pending matches up to a defined maximum capacity
 *  (MaxPlayersPerMatch). Once a match is full, a new pending match is automatically opened.
 *
 *  Concurrency is handled internally via a purely functional Ref state.
 */
final class LobbyManager[F[_]: Sync] private (state: Ref[F, LobbyState]) extends AccessPort[F]:

  def activeMatchIds: F[Set[MatchId]] =
    state.get.map(_.activeMatches.keySet)

  def playersInMatch(matchId: MatchId): F[Set[PlayerId]] =
    state.get.map(_.activeMatches.getOrElse(matchId, Set.empty))

  def pendingMatch: F[Option[MatchId]] =
    state.get.map(_.pendingLobbyMatch)

  def registerMatch(matchId: MatchId): F[Unit] =
    state.update(s => s.copy(activeMatches = s.activeMatches + (matchId -> Set.empty)))

  def removeMatch(matchId: MatchId): F[Unit] =
    state.update: s =>
      s.copy(
        activeMatches = s.activeMatches - matchId,
        pendingLobbyMatch = s.pendingLobbyMatch.filterNot(_ == matchId)
      )

  override def joinLobby(playerId: PlayerId): F[MatchId] =
    Sync[F].delay(MatchId.random()).flatMap: candidateMatchId =>
      state.modify: s =>
        val (matchId, players) = s.pendingLobbyMatch match
          case Some(id) => id -> (s.activeMatches.getOrElse(id, Set.empty) + playerId)
          case None => candidateMatchId -> Set(playerId)

        val isFull = players.size >= LobbyManager.MaxPlayersPerMatch
        val newState = LobbyState(
          activeMatches = s.activeMatches.updated(matchId, players),
          pendingLobbyMatch = Option.unless(isFull)(matchId)
        )
        newState -> matchId

  override def leaveLobby(matchId: MatchId, playerId: PlayerId): F[Unit] =
    state.update: s =>
      s.activeMatches.get(matchId).map(_ - playerId) match
        case Some(remainingPlayers) if remainingPlayers.isEmpty =>
          s.copy(
            activeMatches = s.activeMatches - matchId,
            pendingLobbyMatch = s.pendingLobbyMatch.filterNot(_ == matchId)
          )
        case Some(remainingPlayers) =>
          s.copy(activeMatches = s.activeMatches.updated(matchId, remainingPlayers))
        case None =>
          s

object LobbyManager:
  val MaxPlayersPerMatch: Int = 4

  def of[F[_]: Sync]: F[LobbyManager[F]] =
    Ref.of[F, LobbyState](LobbyState.empty).map(new LobbyManager[F](_))
