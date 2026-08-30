scalaVersion := "3.3.3"
organization := "com.unibo.scalaparty"
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.20" % Test
)

// --- CORE MODULE ---
lazy val core = (project in file("core"))
  .settings(
    name := "scalaparty-core",
  )

// --- INFRASTRUCTURE MODULE ---
lazy val infrastructure = (project in file("infrastructure"))
  .dependsOn(core)
  .settings(
    name := "scalaparty-infrastructure",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "0.23.23",
      "org.http4s" %% "http4s-dsl" % "0.23.23",
      "org.http4s" %% "http4s-circe" % "0.23.23",
      "org.typelevel" %% "cats-effect" % "3.6.3",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.8.0" % Test
    )
  )

lazy val root = (project in file("."))
  .aggregate(core, infrastructure)
  .settings(
    name := "scalaparty"
  )
