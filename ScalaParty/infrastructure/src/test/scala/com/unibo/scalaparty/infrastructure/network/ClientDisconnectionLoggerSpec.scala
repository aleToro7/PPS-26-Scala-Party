package com.unibo.scalaparty.infrastructure.network

import java.io.IOException
import java.util.concurrent.TimeoutException

import cats.effect.{IO, Ref}
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.CompositeFailure
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.Logger

class ClientDisconnectionLoggerSpec extends AsyncWordSpec with AsyncIOSpec with Matchers:

  private type Entry = (String, Option[Throwable], String)

  /** A logger remembering the level, error and message of everything it is asked to log. */
  private class RecordingLogger(entries: Ref[IO, List[Entry]]) extends Logger[IO]:
    private def record(level: String, t: Option[Throwable], message: String) =
      entries.update(_ :+ (level, t, message))

    override def error(t: Throwable)(message: => String): IO[Unit] = record("error", Some(t), message)
    override def error(message: => String): IO[Unit] = record("error", None, message)
    override def warn(t: Throwable)(message: => String): IO[Unit] = record("warn", Some(t), message)
    override def warn(message: => String): IO[Unit] = record("warn", None, message)
    override def info(t: Throwable)(message: => String): IO[Unit] = record("info", Some(t), message)
    override def info(message: => String): IO[Unit] = record("info", None, message)
    override def debug(t: Throwable)(message: => String): IO[Unit] = record("debug", Some(t), message)
    override def debug(message: => String): IO[Unit] = record("debug", None, message)
    override def trace(t: Throwable)(message: => String): IO[Unit] = record("trace", Some(t), message)
    override def trace(message: => String): IO[Unit] = record("trace", None, message)

  private def logged(log: Logger[IO] => IO[Unit]): IO[List[Entry]] =
    for
      entries <- Ref.of[IO, List[Entry]](Nil)
      _       <- log(ClientDisconnectionLogger(RecordingLogger(entries)))
      result  <- entries.get
    yield result

  "ClientDisconnectionLogger".should:

    "log a connection reset by the client at debug level".in:
      val reset = IOException("Connection reset")
      logged(_.error(reset)("WebSocket connection terminated with exception")).asserting:
        _ shouldBe List(("debug", Some(reset), "WebSocket connection terminated with exception"))

    "recognise how every platform reports a client that has gone".in:
      val messages = List("Connection reset", "Connection reset by peer", "Broken pipe")
      IO:
        messages.map(IOException(_)).forall(ClientDisconnectionLogger.isClientDisconnection) shouldBe true

    "recognise a client that has gone while being both read from and written to".in:
      val both = CompositeFailure(IOException("Connection reset"), IOException("Broken pipe"))
      val mixed = CompositeFailure(IOException("Connection reset"), IllegalStateException("bug"))
      IO:
        ClientDisconnectionLogger.isClientDisconnection(both) shouldBe true
        ClientDisconnectionLogger.isClientDisconnection(mixed) shouldBe false

    "keep logging any other error as an error".in:
      val timeout = TimeoutException("60 seconds")
      val failure = IOException("No space left on device")
      logged(logger => logger.error(timeout)("timed out") *> logger.error(failure)("failed")).asserting:
        _ shouldBe List(("error", Some(timeout), "timed out"), ("error", Some(failure), "failed"))

    "pass every other message through unchanged".in:
      logged(logger => logger.error("broken") *> logger.info("started") *> logger.debug("detail")).asserting:
        _ shouldBe List(("error", None, "broken"), ("info", None, "started"), ("debug", None, "detail"))
