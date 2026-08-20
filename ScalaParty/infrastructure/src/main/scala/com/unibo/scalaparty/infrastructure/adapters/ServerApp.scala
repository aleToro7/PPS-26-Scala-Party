package com.unibo.scalaparty.infrastructure.adapters

import cats.effect.{IO, IOApp}
import com.comcast.ip4s._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object ServerApp extends IOApp.Simple {

  val baseRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok("Scala Party Server is up and running!")
  }

  // Router che mappa la rotta sulla root "/"
  val httpApp = Router(
    "/" -> baseRoute
  ).orNotFound

  val run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8081")
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never) // Mantiene il server in esecuzione indefinitamente
}