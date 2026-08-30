package com.unibo.scalaparty.infrastructure.network

import cats.effect.{IO, Ref}
import cats.effect.std.Queue
import org.http4s.websocket.WebSocketFrame
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

type MessageQueue = Queue[IO, WebSocketFrame]

/** Registry for managing active physical connections (e.g., WebSockets).
 *  Tracks the relationship between a connected player and their assigned match,
 *  enabling targeted message broadcasting to specific groups of clients.
 */
trait ConnectionRegistry:

  def bindSessionToMatch(playerId: PlayerId, matchId: MatchId, queue: MessageQueue): IO[Unit]

  /** Removes a physical session from the registry upon disconnection. */
  def removeSession(playerId: PlayerId): IO[Unit]

  /** Retrieves all currently connected players for a given match.
   *  Essential for broadcasting game state updates only to the relevant clients.
   *
   *  @param matchId The match to query.
   *  @return A list of players currently holding an active connection in that match.
   */
  def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]]

  def getQueuesForMatch(matchId: MatchId): IO[List[MessageQueue]]

object ConnectionRegistry:
  private case class Session(matchId: MatchId, queue: MessageQueue)
  private type RegistryState = Map[PlayerId, Session]

  private class ConnectionRegistryImpl(state: Ref[IO, RegistryState]) extends ConnectionRegistry:

    override def bindSessionToMatch(playerId: PlayerId, matchId: MatchId, queue: MessageQueue): IO[Unit] =
      state.update(_ + (playerId -> Session(matchId, queue)))

    override def removeSession(playerId: PlayerId): IO[Unit] =
      state.update(_ - playerId)

    override def getClientsForMatch(matchId: MatchId): IO[List[PlayerId]] =
      state.get.map(_.collect { case (pId, Session(mId, _)) if mId == matchId => pId }.toList)

    override def getQueuesForMatch(matchId: MatchId): IO[List[MessageQueue]] =
      state.get.map(_.values.collect { case Session(mId, q) if mId == matchId => q }.toList)

  def apply(): IO[ConnectionRegistry] =
    Ref.of[IO, RegistryState](Map.empty).map(new ConnectionRegistryImpl(_))
