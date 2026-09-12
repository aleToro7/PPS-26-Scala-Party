package com.unibo.scalaparty.infrastructure.application

import cats.effect.{Ref, Sync}
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{ActiveMatch, JoinOutcome, MatchId, PlayerId}

/** The players waiting for their turn, together with the single match being played, if any.
 *
 *  Invariant: a player either waits in the queue or takes part in the active match, never both.
 */
private final case class WaitingRoom(queue: Vector[PlayerId], active: Option[ActiveMatch]):

  /** Whether the player is taking part in the match being played. */
  def isPlaying(playerId: PlayerId): Boolean =
    active.exists(_.players.contains(playerId))

  /** How many players are served before the given one, or None if it is not waiting. */
  def playersAhead(playerId: PlayerId): Option[Int] =
    queue.indexOf(playerId) match
      case -1 => None
      case position => Some(position)

  /** Appends the player to the back of the queue, unless it is already waiting or playing. */
  def enqueue(playerId: PlayerId): WaitingRoom =
    if playersAhead(playerId).isDefined || isPlaying(playerId) then this
    else copy(queue = queue :+ playerId)

  /** Drops the player from wherever it stands, disbanding the match if that leaves it empty. */
  def remove(playerId: PlayerId): WaitingRoom =
    WaitingRoom(
      queue.filterNot(_ == playerId),
      active.map(m => m.copy(players = m.players - playerId)).filter(_.players.nonEmpty)
    )

  /** Fills the arena from the head of the queue, provided nobody is playing and enough players are
   *  waiting, and reports the match that has just begun.
   */
  def startMatch(candidateId: MatchId, minPlayers: Int, maxPlayers: Int): (WaitingRoom, Option[ActiveMatch]) =
    if active.isDefined || queue.size < minPlayers then (this, None)
    else
      val (picked, rest) = queue.splitAt(maxPlayers)
      val started = ActiveMatch(candidateId, picked.toSet)
      (WaitingRoom(rest, Some(started)), Some(started))

private object WaitingRoom:
  val empty: WaitingRoom = WaitingRoom(Vector.empty, None)

/** Application service handing the arena to one group of players at a time.
 *
 *  Players are served first-come-first-served: as soon as the arena is free and at least
 *  `minPlayers` of them are waiting, up to `maxPlayers` are taken from the head of the queue and a
 *  match begins with exactly those. A match is therefore played by however many players happen to
 *  be around, anywhere from `minPlayers` to `maxPlayers`. Everybody else keeps waiting until that
 *  match is over, which makes disconnecting nothing more than dropping a player out of the queue.
 *
 *  This is the single-match counterpart of [[LobbyManager]], which instead keeps several matches
 *  going side by side and admits players into a lobby until it fills up.
 *
 *  Concurrency is handled internally via a purely functional Ref state.
 */
final class QueuedLobbyManager[F[_]: Sync] private (
    state: Ref[F, WaitingRoom],
    minPlayers: Int,
    maxPlayers: Int
):

  /** The match being played, if the arena is busy. */
  def currentMatch: F[Option[ActiveMatch]] =
    state.get.map(_.active)

  /** The players waiting for their turn, in the order they will be served. */
  def waitingPlayers: F[Vector[PlayerId]] =
    state.get.map(_.queue)

  /** How many players are served before the given one, or None if it is not waiting. */
  def playersAhead(playerId: PlayerId): F[Option[Int]] =
    state.get.map(_.playersAhead(playerId))

  /** Lets a player in, either straight into a match beginning right now or into the queue. */
  def join(playerId: PlayerId): F[JoinOutcome] =
    withCandidateMatchId: candidateId =>
      state.modify: room =>
        val (updated, _) = room.enqueue(playerId).startMatch(candidateId, minPlayers, maxPlayers)
        val outcome = updated.active.filter(_.players.contains(playerId)) match
          case Some(activeMatch) => JoinOutcome.Playing(activeMatch)
          case None => JoinOutcome.Queued(updated.playersAhead(playerId).getOrElse(0))
        updated -> outcome

  /** Drops a player, whether it was waiting or playing, and hands the arena to the next group if
   *  that empties the match it was in. Reports the match that has just begun, if any.
   */
  def leave(playerId: PlayerId): F[Option[ActiveMatch]] =
    withCandidateMatchId: candidateId =>
      state.modify(_.remove(playerId).startMatch(candidateId, minPlayers, maxPlayers))

  /** Declares the given match over and hands the arena to the next group of players. Has no effect
   *  unless the given match is the one being played. Reports the match that has just begun, if any.
   */
  def finishMatch(matchId: MatchId): F[Option[ActiveMatch]] =
    withCandidateMatchId: candidateId =>
      state.modify: room =>
        if !room.active.exists(_.matchId == matchId) then room -> None
        else room.copy(active = None).startMatch(candidateId, minPlayers, maxPlayers)

  /** Runs the given operation with a fresh identifier, to be spent only if a match begins. */
  private def withCandidateMatchId[A](operation: MatchId => F[A]): F[A] =
    Sync[F].delay(MatchId.random()).flatMap(operation)

object QueuedLobbyManager:
  /** The fewest players a match can be played with. */
  val MinPlayersPerMatch: Int = 1

  /** The most players a match can host. */
  val MaxPlayersPerMatch: Int = 4

  /** Builds a manager serving matches of the given size.
   *
   *  @param minPlayers How many players must be waiting before a match begins.
   *  @param maxPlayers How many players a match hosts at most.
   */
  def of[F[_]: Sync](
      minPlayers: Int = MinPlayersPerMatch,
      maxPlayers: Int = MaxPlayersPerMatch
  ): F[QueuedLobbyManager[F]] =
    val valid = MinPlayersPerMatch <= minPlayers && minPlayers <= maxPlayers && maxPlayers <= MaxPlayersPerMatch
    Sync[F].raiseUnless(valid)(
      IllegalArgumentException(
        s"a match hosts from $MinPlayersPerMatch to $MaxPlayersPerMatch players, got $minPlayers to $maxPlayers"
      )
    ) *> Ref.of[F, WaitingRoom](WaitingRoom.empty).map(new QueuedLobbyManager[F](_, minPlayers, maxPlayers))
