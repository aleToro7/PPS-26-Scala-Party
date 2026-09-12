package com.unibo.scalaparty.infrastructure.application

import cats.effect.{Ref, Sync}
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{MatchId, MatchStatus, PlayerId}
import com.unibo.scalaparty.infrastructure.ports.AccessPort

private final case class MatchInfo(players: Set[PlayerId], status: MatchStatus)

/** Snapshot of every match known to the lobby, indexed by its identifier.
 *
 *  Invariant: at most one match sits in the [[MatchStatus.Waiting]] phase at any time,
 *  since a new lobby is opened only when no other one is admitting players.
 */
private final case class LobbyState(matches: Map[MatchId, MatchInfo]):

  /** The only match still admitting players, if one is open. */
  def waiting: Option[(MatchId, MatchInfo)] =
    matches.find((_, info) => info.status == MatchStatus.Waiting)

private object LobbyState:
  val empty: LobbyState = LobbyState(Map.empty)

/** Core application service managing the logical state of the matchmaking lobby.
 *  Groups incoming players into the open lobby up to a defined maximum capacity
 *  (MaxPlayersPerMatch). Once that capacity is reached the match moves to
 *  [[MatchStatus.Running]] and the next joining player opens a new lobby.
 *
 *  Concurrency is handled internally via a purely functional Ref state.
 */
final class LobbyManager[F[_]: Sync] private (state: Ref[F, LobbyState]) extends AccessPort[F]:

  def activeMatchIds: F[Set[MatchId]] =
    state.get.map(_.matches.keySet)

  def playersInMatch(matchId: MatchId): F[Set[PlayerId]] =
    state.get.map(_.matches.get(matchId).fold(Set.empty[PlayerId])(_.players))

  def pendingMatch: F[Option[MatchId]] =
    state.get.map(_.waiting.map(_._1))

  /** Retrieves the lifecycle phase of a match, or None if the match is unknown. */
  def matchStatus(matchId: MatchId): F[Option[MatchStatus]] =
    state.get.map(_.matches.get(matchId).map(_.status))

  /** Marks a match as concluded. Has no effect if the match is unknown. */
  def finishMatch(matchId: MatchId): F[Unit] =
    state.update: s =>
      s.matches.get(matchId) match
        case Some(info) => s.copy(matches = s.matches.updated(matchId, info.copy(status = MatchStatus.Finished)))
        case None => s

  override def joinLobby(playerId: PlayerId): F[MatchId] =
    Sync[F].delay(MatchId.random()).flatMap: candidateMatchId =>
      state.modify: s =>
        val (matchId, players) = s.waiting match
          case Some((id, info)) => id -> (info.players + playerId)
          case None => candidateMatchId -> Set(playerId)

        val status =
          if players.size >= LobbyManager.MaxPlayersPerMatch then MatchStatus.Running else MatchStatus.Waiting

        s.copy(matches = s.matches.updated(matchId, MatchInfo(players, status))) -> matchId

  override def leaveLobby(matchId: MatchId, playerId: PlayerId): F[Unit] =
    state.update: s =>
      s.matches.get(matchId).map(info => info.copy(players = info.players - playerId)) match
        case Some(info) if info.players.isEmpty =>
          s.copy(matches = s.matches - matchId)
        case Some(info) =>
          s.copy(matches = s.matches.updated(matchId, info))
        case None =>
          s

object LobbyManager:
  val MaxPlayersPerMatch: Int = 4

  def of[F[_]: Sync]: F[LobbyManager[F]] =
    Ref.of[F, LobbyState](LobbyState.empty).map(new LobbyManager[F](_))
