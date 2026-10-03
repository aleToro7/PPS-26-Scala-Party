package com.unibo.scalaparty.infrastructure.network

import java.io.IOException

import cats.effect.IO
import fs2.CompositeFailure
import org.typelevel.log4cats.Logger

/** Logger for the HTTP server that reports a client dropping its connection as the ordinary event it
 *  is, rather than as a server error.
 *
 *  The server logs any WebSocket ending with an exception as an error with its whole stack trace,
 *  except for a few cases it recognises. A connection reset by the client is not one of them, though
 *  it is enough for a tab to be killed or the network to go away. Such errors are logged at debug
 *  level instead; every other message goes through unchanged.
 *
 *  @param underlying the logger every message is eventually written to
 */
class ClientDisconnectionLogger(underlying: Logger[IO]) extends Logger[IO]:

  override def error(t: Throwable)(message: => String): IO[Unit] =
    if ClientDisconnectionLogger.isClientDisconnection(t) then underlying.debug(t)(message)
    else underlying.error(t)(message)

  override def error(message: => String): IO[Unit] = underlying.error(message)
  override def warn(t: Throwable)(message: => String): IO[Unit] = underlying.warn(t)(message)
  override def warn(message: => String): IO[Unit] = underlying.warn(message)
  override def info(t: Throwable)(message: => String): IO[Unit] = underlying.info(t)(message)
  override def info(message: => String): IO[Unit] = underlying.info(message)
  override def debug(t: Throwable)(message: => String): IO[Unit] = underlying.debug(t)(message)
  override def debug(message: => String): IO[Unit] = underlying.debug(message)
  override def trace(t: Throwable)(message: => String): IO[Unit] = underlying.trace(t)(message)
  override def trace(message: => String): IO[Unit] = underlying.trace(message)

object ClientDisconnectionLogger:

  /** What the JVM reports when the other end of a socket has gone, depending on the platform. */
  private val DisconnectionMessages = Set("Connection reset", "Connection reset by peer", "Broken pipe")

  /** Tells whether an error only means that the client has dropped its connection.
   *
   *  @param error the error to classify
   *  @return true if the error comes from the client going away, false otherwise
   */
  def isClientDisconnection(error: Throwable): Boolean = error match
    case e: IOException => DisconnectionMessages.contains(e.getMessage)
    // Reading from and writing to the socket may both fail when the client goes, reported together.
    case failure: CompositeFailure => failure.all.forall(isClientDisconnection)
    case _ => false
