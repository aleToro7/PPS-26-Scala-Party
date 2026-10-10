package com.unibo.scalaparty.infrastructure.network

import cats.effect.{IO, Ref}
import cats.effect.std.Queue
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

type MessageQueue = Queue[IO, WebSocketFrame]

/** Registry for managing active physical connections (e.g., WebSockets).
 *  Tracks the relationship between a connected player and their assigned match,
 *  enabling targeted message broadcasting to specific groups of clients.
 *
 *  A connection outlives the match it takes part in: a player is registered as soon as it connects,
 *  possibly with no match at all while it waits for its turn, and is assigned to one later.
 */
trait ConnectionRegistry:

  /** Records a freshly connected player which is not taking part in any match yet. */
  def register(playerId: PlayerId, queue: MessageQueue): IO[Unit]

  def bindSessionToMatch(playerId: PlayerId, matchId: MatchId, queue: MessageQueue): IO[Unit]

  /** Attaches an already registered player to a match. Does nothing if it is not connected. */
  def assignToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit]

  /** Detaches a player from its match, leaving the connection in place. */
  def clearMatch(playerId: PlayerId): IO[Unit]

  /** Removes a physical session from the registry upon disconnection. */
  def removeSession(playerId: PlayerId): IO[Unit]

  /** The match a player is currently taking part in, if any. */
  def matchOf(playerId: PlayerId): IO[Option[MatchId]]

  /** The outbound queue of a single player, to address it on its own. */
  def queueFor(playerId: PlayerId): IO[Option[MessageQueue]]

  /** Retrieves all currently connected players for a given match.
   *  Essential for broadcasting game state updates only to the relevant clients.
   *
   *  @param matchId The match to query.
   *  @return A list of players currently holding an active connection in that match.
   */
  def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]]

  def getQueuesForMatch(matchId: MatchId): IO[List[MessageQueue]]

object ConnectionRegistry:
  private case class Session(matchId: Option[MatchId], queue: MessageQueue)
  private type RegistryState = Map[PlayerId, Session]

  private class ConnectionRegistryImpl(state: Ref[IO, RegistryState]) extends ConnectionRegistry:

    override def register(playerId: PlayerId, queue: MessageQueue): IO[Unit] =
      state.update(_ + (playerId -> Session(None, queue)))

    override def bindSessionToMatch(playerId: PlayerId, matchId: MatchId, queue: MessageQueue): IO[Unit] =
      state.update(_ + (playerId -> Session(Some(matchId), queue)))

    override def assignToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit] =
      state.update(s => s.get(playerId).fold(s)(session => s + (playerId -> session.copy(matchId = Some(matchId)))))

    override def clearMatch(playerId: PlayerId): IO[Unit] =
      state.update(s => s.get(playerId).fold(s)(session => s + (playerId -> session.copy(matchId = None))))

    override def removeSession(playerId: PlayerId): IO[Unit] =
      state.update(_ - playerId)

    override def matchOf(playerId: PlayerId): IO[Option[MatchId]] =
      state.get.map(_.get(playerId).flatMap(_.matchId))

    override def queueFor(playerId: PlayerId): IO[Option[MessageQueue]] =
      state.get.map(_.get(playerId).map(_.queue))

    override def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]] =
      state.get.map(_.collect { case (pId, Session(Some(mId), _)) if mId == matchId => pId }.toList)

    override def getQueuesForMatch(matchId: MatchId): IO[List[MessageQueue]] =
      state.get.map(_.values.collect { case Session(Some(mId), q) if mId == matchId => q }.toList)

  def apply(): IO[ConnectionRegistry] =
    Ref.of[IO, RegistryState](Map.empty).map(new ConnectionRegistryImpl(_))
