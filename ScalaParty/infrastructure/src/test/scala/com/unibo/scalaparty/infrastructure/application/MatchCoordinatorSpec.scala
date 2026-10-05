package com.unibo.scalaparty.infrastructure.application

import scala.concurrent.duration.*

import cats.effect.{Deferred, IO, Ref}
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.*
import com.unibo.scalaparty.core.dto.EntityDto
import com.unibo.scalaparty.core.ecs.EntityId
import com.unibo.scalaparty.core.model.{GameEvent, GameSettings, MatchOutcome, MatchSettings, MatchState}
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

  /** A publisher counting the broadcasts of each match, to tell a ticking match from a stopped one, and
   *  remembering the last state of each, to tell which spaceships are in it.
   */
  private class CountingPublisher(broadcasts: Ref[IO, Map[MatchId, Int]], latest: Ref[IO, Map[MatchId, MatchState]])
      extends MatchEventPublisher[IO]:
    override def broadcastState(matchId: MatchId, state: MatchState): IO[Unit] =
      broadcasts.update(counts => counts.updated(matchId, counts.getOrElse(matchId, 0) + 1)) *>
        latest.update(_.updated(matchId, state))
    override def broadcastEvent(matchId: MatchId, event: GameEvent): IO[Unit] = IO.unit

    def count: IO[Int] = broadcasts.get.map(_.values.sum)

    def countFor(matchId: MatchId): IO[Int] = broadcasts.get.map(_.getOrElse(matchId, 0))

    /** The spaceships in the last state published for the given match. */
    def shipsIn(matchId: MatchId): IO[Set[EntityId]] =
      latest.get.map(_.get(matchId).fold(Set.empty)(_.entities.collect { case ship: EntityDto.Spaceship =>
        ship.id
      }.toSet))

  private object CountingPublisher:
    def apply(): IO[CountingPublisher] =
      (Ref.of[IO, Map[MatchId, Int]](Map.empty), Ref.of[IO, Map[MatchId, MatchState]](Map.empty))
        .mapN(new CountingPublisher(_, _))

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
      timeLimit: Long = 50L,
      minPlayers: Int = 1,
      maxPlayers: Int = 1,
      maxMatches: Int = 1,
      maxQueued: Int = Int.MaxValue
  ): IO[Fixture] =
    for
      registry  <- ConnectionRegistry()
      lobby     <- QueuedLobbyManager.of[IO](minPlayers, maxPlayers, maxMatches, maxQueued)
      commands  <- GameCommandService()
      sent      <- Ref.of[IO, List[(PlayerId, ServerMessage)]](List.empty)
      publisher <- CountingPublisher()
      notifier = RecordingNotifier(sent)
      settings = GameSettings(matchSettings = MatchSettings(timeLimit))
      coordinator <- MatchCoordinator(lobby, registry, commands, notifier, publisher, settings)
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
      yield messages.collect { case ServerMessage.MatchStarted(players, _) => players } shouldBe List(1)

    "tell the playing player which spaceship is its own".in:
      val playerId = PlayerId.random()
      for
        f         <- fixture(timeLimit = 10_000L)
        _         <- f.join(playerId)
        matchId   <- f.registry.matchOf(playerId).map(_.get)
        published <- eventually(f.publisher.shipsIn(matchId))(_.nonEmpty)
        messages  <- f.notifier.messagesFor(playerId)
        _         <- f.coordinator.leaveLobby(playerId)
      yield messages.collect { case ServerMessage.MatchStarted(_, you) => Set(you) } shouldBe List(published)

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
        f          <- fixture(timeLimit = 10_000L, maxQueued = 1)
        admissions <- players.traverse(f.join)
        messages   <- f.notifier.messagesFor(players.last)
      yield
        admissions shouldBe List(Admission.Admitted, Admission.Admitted, Admission.Rejected)
        messages shouldBe List(ServerMessage.QueueFull)

    "keep a rejected player out of the queue and of every match".in:
      val players = List.fill(3)(PlayerId.random())
      for
        f       <- fixture(timeLimit = 10_000L, maxQueued = 1)
        _       <- players.traverse(f.join)
        waiting <- f.lobby.waitingPlayers
        bound   <- f.registry.matchOf(players.last)
      yield
        waiting shouldBe Vector(players(1))
        bound shouldBe None

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

    "tell the players of the finished match how it ended".in:
      val player1 = PlayerId.random()
      val player2 = PlayerId.random()
      val timeUp = ServerMessage.MatchEnded(MatchOutcome.TimeUp)
      for
        f        <- fixture(timeLimit = 50L, minPlayers = 2, maxPlayers = 2)
        _        <- f.join(player1)
        _        <- f.join(player2)
        messages <- eventually(f.notifier.messagesFor(player1))(_.contains(timeUp))
      yield messages should contain(timeUp)

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
        f       <- fixture(timeLimit = 10_000L)
        _       <- f.join(playing)
        _       <- f.join(waiting)
        _       <- f.coordinator.leaveLobby(playing)
        matches <- f.lobby.activeMatches
      yield matches.map(_.players) shouldBe Set(Set(waiting))

    "stop ticking the match once its last player has quit and nobody is waiting".in:
      val playing = PlayerId.random()
      for
        f       <- fixture(timeLimit = 10_000L)
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
        publisher   <- CountingPublisher()
        coordinator <- Deferred[IO, MatchCoordinator]
        // The player quits on hearing that its match has begun, before the match fiber is recorded.
        quitting = new PlayerNotifier[IO]:
          override def send(pId: PlayerId, message: ServerMessage): IO[Unit] = message match
            case ServerMessage.MatchStarted(_, _) => coordinator.get.flatMap(_.leaveLobby(pId))
            case _ => IO.unit
        created <- MatchCoordinator(lobby, registry, commands, quitting, publisher, GameSettings.default)
        _       <- coordinator.complete(created)
        _       <- Queue.unbounded[IO, WebSocketFrame].flatMap(registry.register(playerId, _))
        _       <- created.joinLobby(playerId)
        _       <- IO.sleep(200.millis)
        ticks   <- publisher.count
      yield ticks shouldBe 0

    "tell the players still waiting that they moved up the queue".in:
      val playing = PlayerId.random()
      val giveUp = PlayerId.random()
      val last = PlayerId.random()
      for
        f        <- fixture(timeLimit = 10_000L)
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
        f           <- fixture(timeLimit = 10_000L, maxMatches = 2)
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
        f           <- fixture(timeLimit = 10_000L, maxMatches = 2)
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
        f        <- fixture(timeLimit = 10_000L, maxMatches = 2)
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
        f           <- fixture(timeLimit = 10_000L, maxMatches = 2)
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
        f           <- fixture(timeLimit = 10_000L, maxMatches = 2)
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

  "a match of several players".should:
    "keep the first player waiting until enough players have arrived".in:
      val first = PlayerId.random()
      for
        f        <- fixture(timeLimit = 10_000L, minPlayers = 2, maxPlayers = 2)
        _        <- f.join(first)
        matches  <- f.lobby.activeMatches
        messages <- f.notifier.messagesFor(first)
      yield
        matches shouldBe empty
        messages shouldBe List(ServerMessage.Queued(playersAhead = 0))

    "begin with all of them, telling each one its own spaceship among those in the match".in:
      val players = List.fill(2)(PlayerId.random())
      for
        f         <- fixture(timeLimit = 10_000L, minPlayers = 2, maxPlayers = 2)
        _         <- players.traverse_(f.join)
        matchIds  <- players.traverse(f.registry.matchOf)
        published <- eventually(f.publisher.shipsIn(matchIds.head.get))(_.nonEmpty)
        started   <- players.traverse(f.notifier.messagesFor(_).map(_.collect { case m: ServerMessage.MatchStarted =>
          m
        }))
        _ <- players.traverse_(f.coordinator.leaveLobby)
      yield
        matchIds.distinct should have size 1
        started.map(_.map(_.players)) shouldBe List(List(2), List(2))
        started.flatten.map(_.you).toSet shouldBe published
        published should have size 2

    "go on without the spaceship of a player who quits, for the players left".in:
      val quitting = PlayerId.random()
      val staying = List.fill(2)(PlayerId.random())
      for
        f       <- fixture(timeLimit = 10_000L, minPlayers = 3, maxPlayers = 3)
        _       <- (quitting :: staying).traverse_(f.join)
        matchId <- f.registry.matchOf(quitting).map(_.get)
        ships   <- staying.flatTraverse(f.notifier.messagesFor(_).map(_.collect {
          case ServerMessage.MatchStarted(_, you) => you
        }))
        _       <- eventually(f.publisher.shipsIn(matchId))(_.size == 3)
        _       <- f.coordinator.leaveLobby(quitting)
        left    <- eventually(f.publisher.shipsIn(matchId))(_.size == 2)
        matches <- f.lobby.activeMatches
        _       <- staying.traverse_(f.coordinator.leaveLobby)
      yield
        left shouldBe ships.toSet
        matches.map(_.players) shouldBe Set(staying.toSet)

    "end once a single player is left, telling it that its spaceship won".in:
      val quitting = PlayerId.random()
      val staying = PlayerId.random()
      for
        f        <- fixture(timeLimit = 10_000L, minPlayers = 2, maxPlayers = 2)
        _        <- f.join(quitting)
        _        <- f.join(staying)
        ships    <- f.notifier.messagesFor(staying).map(_.collect { case ServerMessage.MatchStarted(_, you) => you })
        _        <- f.coordinator.leaveLobby(quitting)
        messages <- eventually(f.notifier.messagesFor(staying))(_.exists(_.isInstanceOf[ServerMessage.MatchEnded]))
      yield messages should contain(ServerMessage.MatchEnded(MatchOutcome.LastStanding(ships.head)))
