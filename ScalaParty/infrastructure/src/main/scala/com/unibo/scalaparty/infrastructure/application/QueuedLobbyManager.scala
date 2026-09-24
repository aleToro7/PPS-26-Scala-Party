package com.unibo.scalaparty.infrastructure.application

import cats.effect.{Ref, Sync}
import cats.syntax.all.*
import com.unibo.scalaparty.infrastructure.model.{ActiveMatch, JoinOutcome, LeaveOutcome, MatchId, PlayerId}

/** The players waiting for their turn, together with the matches being played, by identifier.
 *
 *  Invariant: a player either waits in the queue or takes part in exactly one active match.
 */
private final case class WaitingRoom(queue: Vector[PlayerId], active: Map[MatchId, ActiveMatch]):

  /** The match the player is taking part in, if any. */
  def matchOf(playerId: PlayerId): Option[ActiveMatch] =
    active.values.find(_.players.contains(playerId))

  /** How many players are served before the given one, or None if it is not waiting. */
  def playersAhead(playerId: PlayerId): Option[Int] =
    queue.indexOf(playerId) match
      case -1 => None
      case position => Some(position)

  /** Appends the player to the back of the queue, unless it is already waiting or playing. */
  def enqueue(playerId: PlayerId): WaitingRoom =
    if playersAhead(playerId).isDefined || matchOf(playerId).isDefined then this
    else copy(queue = queue :+ playerId)

  /** Drops the player from wherever it stands, disbanding the match if that leaves it empty, and
   *  reports the match so disbanded, if any.
   */
  def remove(playerId: PlayerId): (WaitingRoom, Option[MatchId]) =
    val remaining = active.map((matchId, m) => matchId -> m.copy(players = m.players - playerId))
    val disbanded = remaining.collectFirst { case (matchId, m) if m.players.isEmpty => matchId }
    (WaitingRoom(queue.filterNot(_ == playerId), remaining -- disbanded), disbanded)

  /** Fills a free room from the head of the queue, provided one is left and enough players are
   *  waiting, and reports the match that has just begun.
   *
   *  A single match is enough: every operation adds at most one player to the queue or frees at most
   *  one room, so after each of them either every room is taken or fewer than `minPlayers` wait.
   */
  def startMatch(
      candidateId: MatchId,
      minPlayers: Int,
      maxPlayers: Int,
      maxMatches: Int
  ): (WaitingRoom, Option[ActiveMatch]) =
    if active.size >= maxMatches || queue.size < minPlayers then (this, None)
    else
      val (picked, rest) = queue.splitAt(maxPlayers)
      val started = ActiveMatch(candidateId, picked.toSet)
      (WaitingRoom(rest, active + (candidateId -> started)), Some(started))

private object WaitingRoom:
  val empty: WaitingRoom = WaitingRoom(Vector.empty, Map.empty)

/** Application service sharing a fixed number of rooms among the players, one group per room.
 *
 *  Players are served first-come-first-served: as soon as a room is free and at least `minPlayers`
 *  of them are waiting, up to `maxPlayers` are taken from the head of the queue and a match begins
 *  in that room with exactly those. A match is therefore played by however many players happen to
 *  be around, anywhere from `minPlayers` to `maxPlayers`, and up to `maxMatches` of them are played
 *  side by side. Everybody else keeps waiting until a room is freed, which makes disconnecting
 *  nothing more than dropping a player out of the queue. At most `maxQueued` players wait at once:
 *  whoever arrives with every room taken and the queue full is turned away.
 *
 *  Concurrency is handled internally via a purely functional Ref state. Queue and rooms share a
 *  single Ref on purpose: freeing a room and picking who takes it must be one atomic step, or two
 *  concurrent updates could pick the same players.
 */
final class QueuedLobbyManager[F[_]: Sync] private (
    state: Ref[F, WaitingRoom],
    minPlayers: Int,
    maxPlayers: Int,
    maxMatches: Int,
    maxQueued: Int
):

  /** The matches being played. */
  def activeMatches: F[Set[ActiveMatch]] =
    state.get.map(_.active.values.toSet)

  /** The players waiting for their turn, in the order they will be served. */
  def waitingPlayers: F[Vector[PlayerId]] =
    state.get.map(_.queue)

  /** How many players are served before the given one, or None if it is not waiting. */
  def playersAhead(playerId: PlayerId): F[Option[Int]] =
    state.get.map(_.playersAhead(playerId))

  /** Lets a player in, either straight into a match beginning right now or into the queue, or turns
   *  it away if it would have to wait and the queue is full. A rejected player leaves no trace.
   */
  def join(playerId: PlayerId): F[JoinOutcome] =
    withCandidateMatchId: candidateId =>
      state.modify: room =>
        val (updated, _) = room.enqueue(playerId).startMatch(candidateId, minPlayers, maxPlayers, maxMatches)
        updated.matchOf(playerId) match
          case Some(activeMatch) => updated -> JoinOutcome.Playing(activeMatch)
          case None if updated.queue.size > maxQueued => room -> JoinOutcome.Rejected
          case None => updated -> JoinOutcome.Queued(updated.playersAhead(playerId).getOrElse(0))

  /** Drops a player, whether it was waiting or playing, and hands the room over to the next group if
   *  that empties the match it was in. Reports the match disbanded and the one begun, if any.
   */
  def leave(playerId: PlayerId): F[LeaveOutcome] =
    withCandidateMatchId: candidateId =>
      state.modify: room =>
        val (remaining, disbanded) = room.remove(playerId)
        val (updated, started) = remaining.startMatch(candidateId, minPlayers, maxPlayers, maxMatches)
        updated -> LeaveOutcome(disbanded, started)

  /** Declares the given match over and hands its room to the next group of players. Has no effect
   *  unless the given match is being played. Reports the match that has just begun, if any.
   */
  def finishMatch(matchId: MatchId): F[Option[ActiveMatch]] =
    withCandidateMatchId: candidateId =>
      state.modify: room =>
        if !room.active.contains(matchId) then room -> None
        else room.copy(active = room.active - matchId).startMatch(candidateId, minPlayers, maxPlayers, maxMatches)

  /** Runs the given operation with a fresh identifier, to be spent only if a match begins. */
  private def withCandidateMatchId[A](operation: MatchId => F[A]): F[A] =
    Sync[F].delay(MatchId.random()).flatMap(operation)

object QueuedLobbyManager:
  /** The fewest players a match can be played with. */
  val MinPlayersPerMatch: Int = 1

  /** The most players a match can host. */
  val MaxPlayersPerMatch: Int = 4

  /** Builds a manager serving matches of the given size in the given number of rooms.
   *
   *  The queue must hold at least `minPlayers - 1` players, or a match could never gather enough of
   *  them to begin: the player completing a match is the only one never kept waiting.
   *
   *  @param minPlayers How many players must be waiting before a match begins.
   *  @param maxPlayers How many players a match hosts at most.
   *  @param maxMatches How many matches can be played at the same time.
   *  @param maxQueued  How many players can wait at the same time, unbounded unless given.
   */
  def of[F[_]: Sync](
      minPlayers: Int = MinPlayersPerMatch,
      maxPlayers: Int = MaxPlayersPerMatch,
      maxMatches: Int = 1,
      maxQueued: Int = Int.MaxValue
  ): F[QueuedLobbyManager[F]] =
    val validSize = MinPlayersPerMatch <= minPlayers && minPlayers <= maxPlayers && maxPlayers <= MaxPlayersPerMatch
    for
      _ <- Sync[F].raiseUnless(validSize)(
        IllegalArgumentException(
          s"a match hosts from $MinPlayersPerMatch to $MaxPlayersPerMatch players, got $minPlayers to $maxPlayers"
        )
      )
      _ <- Sync[F].raiseUnless(maxMatches >= 1)(
        IllegalArgumentException(s"at least one match must be allowed to run, got $maxMatches")
      )
      _ <- Sync[F].raiseUnless(maxQueued >= minPlayers - 1)(
        IllegalArgumentException(
          s"the queue must hold at least ${minPlayers - 1} players for a match of $minPlayers, got $maxQueued"
        )
      )
      state <- Ref.of[F, WaitingRoom](WaitingRoom.empty)
    yield new QueuedLobbyManager[F](state, minPlayers, maxPlayers, maxMatches, maxQueued)
