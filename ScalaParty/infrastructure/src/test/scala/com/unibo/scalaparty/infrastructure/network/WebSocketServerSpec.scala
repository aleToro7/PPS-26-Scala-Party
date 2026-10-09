package com.unibo.scalaparty.infrastructure.network

import cats.effect.{IO, Ref}
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import com.unibo.scalaparty.infrastructure.model.{Admission, MatchId, PlayerId}
import com.unibo.scalaparty.infrastructure.network.dto.PlayerInput
import com.unibo.scalaparty.infrastructure.ports.{AccessPort, CommandPort}
import org.http4s.websocket.WebSocketFrame
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import scodec.bits.ByteVector

class WebSocketServerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  /** An access port admitting everybody and remembering who left. */
  private class RecordingAccess(left: Ref[IO, List[PlayerId]]) extends AccessPort[IO]:
    override def joinLobby(playerId: PlayerId): IO[Admission] = IO.pure(Admission.Admitted)
    override def leaveLobby(playerId: PlayerId): IO[Unit] = left.update(_ :+ playerId)

    def leftPlayers: IO[List[PlayerId]] = left.get

  /** A command port remembering every command it was handed. */
  private class RecordingCommands(handled: Ref[IO, List[(MatchId, PlayerId, PlayerInput)]]) extends CommandPort[IO]:
    override def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerInput): IO[Unit] =
      handled.update(_ :+ (matchId, playerId, command))

    def commands: IO[List[(MatchId, PlayerId, PlayerInput)]] = handled.get

  /** The server under test, with its registry and recording ports at hand. */
  private class Fixture(
      val registry: ConnectionRegistry,
      val access: RecordingAccess,
      val commands: RecordingCommands,
      val server: WebSocketServer
  ):
    /** Connects a player through the server, as a new socket would do. */
    def connect(playerId: PlayerId): IO[Unit] =
      Queue.unbounded[IO, WebSocketFrame].flatMap(server.onConnect(playerId, _)).void

    /** Connects a player and assigns it to the given match, as the coordinator would do. */
    def connectToMatch(playerId: PlayerId, matchId: MatchId): IO[Unit] =
      connect(playerId) *> registry.assignToMatch(playerId, matchId)

  private def fixture: IO[Fixture] =
    for
      registry <- ConnectionRegistry()
      left     <- Ref.of[IO, List[PlayerId]](List.empty)
      handled  <- Ref.of[IO, List[(MatchId, PlayerId, PlayerInput)]](List.empty)
      access = RecordingAccess(left)
      commands = RecordingCommands(handled)
    yield Fixture(registry, access, commands, WebSocketServer(registry, access, commands))

  "onMessage".should:
    "route a valid input to the match of the player who sent it".in:
      val playerId = PlayerId.random()
      val matchId = MatchId.random()
      for
        f        <- fixture
        _        <- f.connectToMatch(playerId, matchId)
        _        <- f.server.onMessage(playerId, WebSocketFrame.Text("""{"Rotate":{"angle":45.0}}"""))
        commands <- f.commands.commands
      yield commands shouldBe List((matchId, playerId, PlayerInput.Rotate(45.0)))

    "drop the input of a player still waiting for a match".in:
      val playerId = PlayerId.random()
      for
        f        <- fixture
        _        <- f.connect(playerId)
        _        <- f.server.onMessage(playerId, WebSocketFrame.Text("""{"Shoot":{}}"""))
        commands <- f.commands.commands
      yield commands shouldBe empty

    "ignore a message that is not a valid input".in:
      val playerId = PlayerId.random()
      for
        f        <- fixture
        _        <- f.connectToMatch(playerId, MatchId.random())
        _        <- f.server.onMessage(playerId, WebSocketFrame.Text("""{"Teleport":{"x":1}}"""))
        _        <- f.server.onMessage(playerId, WebSocketFrame.Text("not even JSON"))
        commands <- f.commands.commands
      yield commands shouldBe empty

    "ignore any frame that is not text".in:
      val playerId = PlayerId.random()
      for
        f        <- fixture
        _        <- f.connectToMatch(playerId, MatchId.random())
        _        <- f.server.onMessage(playerId, WebSocketFrame.Binary(ByteVector(1, 2, 3)))
        _        <- f.server.onMessage(playerId, WebSocketFrame.Pong())
        commands <- f.commands.commands
      yield commands shouldBe empty

  "onDisconnect".should:
    "forget the session of the player".in:
      val playerId = PlayerId.random()
      for
        f       <- fixture
        _       <- f.connectToMatch(playerId, MatchId.random())
        _       <- f.server.onDisconnect(playerId)
        session <- f.registry.queueFor(playerId)
        matchOf <- f.registry.matchOf(playerId)
      yield
        session shouldBe None
        matchOf shouldBe None

    "make the player leave the lobby".in:
      val playerId = PlayerId.random()
      for
        f    <- fixture
        _    <- f.connect(playerId)
        _    <- f.server.onDisconnect(playerId)
        left <- f.access.leftPlayers
      yield left shouldBe List(playerId)
