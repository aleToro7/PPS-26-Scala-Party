package com.unibo.scalaparty.infrastructure.application

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.duration.*

import cats.effect.{Deferred, IO, Ref}
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.core.map.{GameMap, GameMapProvider}
import com.unibo.scalaparty.core.model.{GameEvent, GameSettings, MatchState}
import com.unibo.scalaparty.infrastructure.model.{Admission, MatchId, PlayerId, ServerMessage}
import com.unibo.scalaparty.infrastructure.network.ConnectionRegistry
import com.unibo.scalaparty.infrastructure.ports.{MatchEventPublisher, PlayerNotifier}
import org.http4s.websocket.WebSocketFrame
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class MatchCoordinatorSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  /** A notifier remembering everything it was asked to deliver. */
  private class RecordingNotifier(sent: Ref[IO, List[(PlayerId, ServerMessage)]]) extends PlayerNotifier[IO]:
    override def send(playerId: PlayerId, message: ServerMessage): IO[Unit] =
      sent.update(_ :+ (playerId -> message))

    def messagesFor(playerId: PlayerId): IO[List[ServerMessage]] =
      sent.get.map(_.collect { case (pId, message) if pId == playerId => message })

  /** A publisher counting the broadcasts of each match, to tell a ticking match from a stopped one. */
  private class CountingPublisher(broadcasts: Ref[IO, Map[MatchId, Int]]) extends MatchEventPublisher[IO]:
    override def broadcastState(matchId: MatchId, state: MatchState): IO[Unit] =
      broadcasts.update(counts => counts.updated(matchId, counts.getOrElse(matchId, 0) + 1))
    override def broadcastEvent(matchId: MatchId, event: GameEvent): IO[Unit] = IO.unit

    def count: IO[Int] = broadcasts.get.map(_.values.sum)

    def countFor(matchId: MatchId): IO[Int] = broadcasts.get.map(_.getOrElse(matchId, 0))

  /** A map provider giving the same answer to every request, remembering how many players each map was requested for. */
  private class RecordingMapProvider(answer: Option[GameMap]) extends GameMapProvider:
    private val requests = AtomicReference(List.empty[Int])

    override def mapFor(players: Int): Option[GameMap] =
      requests.updateAndGet(_ :+ players)
      answer

    def requested: IO[List[Int]] = IO(requests.get)

  /** The whole wiring under test, with matches short enough to watch them come and go. */
  private class Fixture(
      val lobby: QueuedLobbyManager[IO],
      val registry: ConnectionRegistry,
      val notifier: RecordingNotifier,
      val publisher: CountingPublisher,
      val coordinator: MatchCoordinator
  ):
    /** Registers a connection, as the WebSocket adapter would do before joining. */
    def connect(playerId: PlayerId): IO[Unit] =
      Queue.unbounded[IO, WebSocketFrame].flatMap(registry.register(playerId, _))

    def join(playerId: PlayerId): IO[Admission] =
      connect(playerId) *> coordinator.joinLobby(playerId)

  private def fixture(
      matchDuration: FiniteDuration = 50.millis,
      maxPlayers: Int = 1,
      maxMatches: Int = 1,
      maxQueued: Int = Int.MaxValue,
      maps: GameMapProvider = GameMapProvider.fixed(GameMap.default)
  ): IO[Fixture] =
    for
      registry   <- ConnectionRegistry()
      lobby      <- QueuedLobbyManager.of[IO](maxPlayers = maxPlayers, maxMatches = maxMatches, maxQueued = maxQueued)
      commands   <- GameCommandService()
      sent       <- Ref.of[IO, List[(PlayerId, ServerMessage)]](List.empty)
      broadcasts <- Ref.of[IO, Map[MatchId, Int]](Map.empty)
      notifier = RecordingNotifier(sent)
      publisher = CountingPublisher(broadcasts)
      settings = GameSettings.default
      coordinator <- MatchCoordinator(lobby, registry, commands, notifier, publisher, settings, maps, matchDuration)
    yield Fixture(lobby, registry, notifier, publisher, coordinator)

  /** Retries the given check until it holds, rather than guessing how long a match takes. */
  private def eventually[A](action: IO[A])(predicate: A => Boolean): IO[A] =
    action
      .flatMap(value =>
        if predicate(value) then IO.pure(value) else IO.sleep(10.millis) *> eventually(action)(predicate)
      )
      .timeout(10.seconds)

  "joining".should:
    "start a match right away for the first player".in:
      val playerId = PlayerId.random()
      for
        f       <- fixture()
        _       <- f.join(playerId)
        matches <- f.lobby.activeMatches
      yield matches.map(_.players) shouldBe Set(Set(playerId))

    "bind the playing player to its match in the registry".in:
      val playerId = PlayerId.random()
      for
        f       <- fixture()
        _       <- f.join(playerId)
        matches <- f.lobby.activeMatches
        bound   <- f.registry.matchOf(playerId)
      yield bound shouldBe matches.headOption.map(_.matchId)

    "tell the playing player that its match has begun".in:
      val playerId = PlayerId.random()
      for
        f        <- fixture()
        _        <- f.join(playerId)
        messages <- f.notifier.messagesFor(playerId)
      yield messages should contain(ServerMessage.MatchStarted(players = 1))

    "tell a player arriving during a match how many are ahead of it".in:
      val playing = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f        <- fixture()
        _        <- f.join(playing)
        _        <- f.join(waiting)
        messages <- f.notifier.messagesFor(waiting)
      yield messages should contain(ServerMessage.Queued(playersAhead = 0))

    "leave a waiting player out of the running match".in:
      val playing = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f     <- fixture()
        _     <- f.join(playing)
        _     <- f.join(waiting)
        bound <- f.registry.matchOf(waiting)
      yield bound shouldBe None

    "admit a player it can host".in:
      for
        f         <- fixture()
        admission <- f.join(PlayerId.random())
      yield admission shouldBe Admission.Admitted

    "turn a player away once the queue is full, telling it why".in:
      val players = List.fill(3)(PlayerId.random())
      for
        f          <- fixture(matchDuration = 10.seconds, maxQueued = 1)
        admissions <- players.traverse(f.join)
        messages   <- f.notifier.messagesFor(players.last)
      yield
        admissions shouldBe List(Admission.Admitted, Admission.Admitted, Admission.Rejected)
        messages shouldBe List(ServerMessage.QueueFull)

    "keep a rejected player out of the queue and of every match".in:
      val players = List.fill(3)(PlayerId.random())
      for
        f       <- fixture(matchDuration = 10.seconds, maxQueued = 1)
        _       <- players.traverse(f.join)
        waiting <- f.lobby.waitingPlayers
        bound   <- f.registry.matchOf(players.last)
      yield
        waiting shouldBe Vector(players(1))
        bound shouldBe None

  "the start of a match".should:
    "ask for a map hosting the players of the match".in:
      val maps = RecordingMapProvider(Some(GameMap.default))
      for
        f         <- fixture(maps = maps)
        _         <- f.join(PlayerId.random())
        requested <- eventually(maps.requested)(_.nonEmpty)
      yield requested shouldBe List(1)

    "end the match without playing it when no map can host its players".in:
      val playerId = PlayerId.random()
      for
        f     <- fixture(matchDuration = 10.seconds, maps = RecordingMapProvider(None))
        _     <- f.join(playerId)
        _     <- eventually(f.notifier.messagesFor(playerId))(_.contains(ServerMessage.MatchEnded))
        bound <- f.registry.matchOf(playerId)
        ticks <- f.publisher.count
      yield
        bound shouldBe None
        ticks shouldBe 0

    "end the match without playing it when providing a map fails".in:
      val playerId = PlayerId.random()
      val failing: GameMapProvider = _ => throw IllegalStateException("Malformed game map")
      for
        f     <- fixture(matchDuration = 10.seconds, maps = failing)
        _     <- f.join(playerId)
        _     <- eventually(f.notifier.messagesFor(playerId))(_.contains(ServerMessage.MatchEnded))
        bound <- f.registry.matchOf(playerId)
        ticks <- f.publisher.count
      yield
        bound shouldBe None
        ticks shouldBe 0

    "hand the room of a match it cannot play to the player waiting in the queue".in:
      val first = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f    <- fixture(matchDuration = 10.seconds, maps = RecordingMapProvider(None))
        _    <- f.join(first)
        _    <- f.join(waiting)
        next <- eventually(f.notifier.messagesFor(waiting))(_.contains(ServerMessage.MatchStarted(players = 1)))
      yield next should contain(ServerMessage.MatchStarted(players = 1))

  "the end of a match".should:
    "hand the arena to the player waiting in the queue".in:
      val playing = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f    <- fixture()
        _    <- f.join(playing)
        _    <- f.join(waiting)
        next <- eventually(f.lobby.activeMatches)(_.exists(_.players == Set(waiting)))
      yield next.map(_.players) shouldBe Set(Set(waiting))

    "tell the players of the finished match that it is over".in:
      val playing = PlayerId.random()
      for
        f        <- fixture()
        _        <- f.join(playing)
        messages <- eventually(f.notifier.messagesFor(playing))(_.contains(ServerMessage.MatchEnded))
      yield messages should contain(ServerMessage.MatchEnded)

    "leave the arena free when nobody else is waiting".in:
      val playing = PlayerId.random()
      for
        f       <- fixture()
        _       <- f.join(playing)
        matches <- eventually(f.lobby.activeMatches)(_.isEmpty)
      yield matches shouldBe empty

    "release the finished players from their match in the registry".in:
      val playing = PlayerId.random()
      for
        f     <- fixture()
        _     <- f.join(playing)
        _     <- eventually(f.lobby.activeMatches)(_.isEmpty)
        bound <- f.registry.matchOf(playing)
      yield bound shouldBe None

  "leaving".should:
    "drop a waiting player so it is never picked for a match".in:
      val playing = PlayerId.random()
      val giveUp = PlayerId.random()
      for
        f       <- fixture()
        _       <- f.join(playing)
        _       <- f.join(giveUp)
        _       <- f.coordinator.leaveLobby(giveUp)
        waiting <- f.lobby.waitingPlayers
        matches <- eventually(f.lobby.activeMatches)(_.isEmpty)
      yield
        waiting shouldBe empty
        matches shouldBe empty

    "hand the arena over at once when the playing player quits".in:
      val playing = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f       <- fixture(matchDuration = 10.seconds)
        _       <- f.join(playing)
        _       <- f.join(waiting)
        _       <- f.coordinator.leaveLobby(playing)
        matches <- f.lobby.activeMatches
      yield matches.map(_.players) shouldBe Set(Set(waiting))

    "stop ticking the match once its last player has quit and nobody is waiting".in:
      val playing = PlayerId.random()
      for
        f       <- fixture(matchDuration = 10.seconds)
        _       <- f.join(playing)
        _       <- f.coordinator.leaveLobby(playing)
        settled <- f.publisher.count
        _       <- IO.sleep(200.millis)
        later   <- f.publisher.count
      yield later shouldBe settled

    "stop the match its only player quit while it was being started".in:
      val playerId = PlayerId.random()
      for
        registry    <- ConnectionRegistry()
        lobby       <- QueuedLobbyManager.of[IO](maxPlayers = 1, maxMatches = 1)
        commands    <- GameCommandService()
        broadcasts  <- Ref.of[IO, Map[MatchId, Int]](Map.empty)
        coordinator <- Deferred[IO, MatchCoordinator]
        // The player quits on hearing that its match has begun, before the match fiber is recorded.
        quitting = new PlayerNotifier[IO]:
          override def send(pId: PlayerId, message: ServerMessage): IO[Unit] = message match
            case ServerMessage.MatchStarted(_) => coordinator.get.flatMap(_.leaveLobby(pId))
            case _ => IO.unit
        publisher = CountingPublisher(broadcasts)
        maps = GameMapProvider.fixed(GameMap.default)
        created <-
          MatchCoordinator(lobby, registry, commands, quitting, publisher, GameSettings.default, maps, 10.seconds)
        _     <- coordinator.complete(created)
        _     <- Queue.unbounded[IO, WebSocketFrame].flatMap(registry.register(playerId, _))
        _     <- created.joinLobby(playerId)
        _     <- IO.sleep(200.millis)
        ticks <- publisher.count
      yield ticks shouldBe 0

    "tell the players still waiting that they moved up the queue".in:
      val playing = PlayerId.random()
      val giveUp = PlayerId.random()
      val last = PlayerId.random()
      for
        f        <- fixture(matchDuration = 10.seconds)
        _        <- f.join(playing)
        _        <- f.join(giveUp)
        _        <- f.join(last)
        _        <- f.coordinator.leaveLobby(giveUp)
        messages <- f.notifier.messagesFor(last)
      yield messages.last shouldBe ServerMessage.Queued(playersAhead = 0)

  "several matches".should:
    "run a match of its own for each player while rooms are free".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        f           <- fixture(matchDuration = 10.seconds, maxMatches = 2)
        _           <- f.join(first)
        _           <- f.join(second)
        firstBound  <- f.registry.matchOf(first)
        secondBound <- f.registry.matchOf(second)
        matches     <- f.lobby.activeMatches
      yield
        firstBound should not be secondBound
        matches.map(m => Option(m.matchId)) shouldBe Set(firstBound, secondBound)

    "tick every match being played".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      for
        f           <- fixture(matchDuration = 10.seconds, maxMatches = 2)
        _           <- f.join(first)
        _           <- f.join(second)
        firstMatch  <- f.registry.matchOf(first)
        secondMatch <- f.registry.matchOf(second)
        ticked      <- eventually(List(firstMatch, secondMatch).flatten.traverse(f.publisher.countFor))(
          _.forall(_ > 0)
        )
      yield ticked should have size 2

    "queue the players arriving once every room is taken".in:
      val players = List.fill(3)(PlayerId.random())
      for
        f        <- fixture(matchDuration = 10.seconds, maxMatches = 2)
        _        <- players.traverse_(f.join)
        messages <- f.notifier.messagesFor(players.last)
        bound    <- f.registry.matchOf(players.last)
      yield
        messages should contain(ServerMessage.Queued(playersAhead = 0))
        bound shouldBe None

    "hand the room of a quitting player over while the other match keeps going".in:
      val first = PlayerId.random()
      val second = PlayerId.random()
      val waiting = PlayerId.random()
      for
        f           <- fixture(matchDuration = 10.seconds, maxMatches = 2)
        _           <- f.join(first)
        _           <- f.join(second)
        _           <- f.join(waiting)
        secondMatch <- f.registry.matchOf(second)
        _           <- f.coordinator.leaveLobby(first)
        matches     <- f.lobby.activeMatches
        stillBound  <- f.registry.matchOf(second)
      yield
        matches.map(_.players) shouldBe Set(Set(second), Set(waiting))
        stillBound shouldBe secondMatch

    "stop ticking only the match its last player has quit".in:
      val quitting = PlayerId.random()
      val staying = PlayerId.random()
      for
        f           <- fixture(matchDuration = 10.seconds, maxMatches = 2)
        _           <- f.join(quitting)
        _           <- f.join(staying)
        quitMatch   <- f.registry.matchOf(quitting).map(_.get)
        stayMatch   <- f.registry.matchOf(staying).map(_.get)
        _           <- f.coordinator.leaveLobby(quitting)
        quitSettled <- f.publisher.countFor(quitMatch)
        staySettled <- f.publisher.countFor(stayMatch)
        _           <- IO.sleep(200.millis)
        quitLater   <- f.publisher.countFor(quitMatch)
        stayLater   <- f.publisher.countFor(stayMatch)
      yield
        quitLater shouldBe quitSettled
        stayLater should be > staySettled
