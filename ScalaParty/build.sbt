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
      "org.http4s"      %% "http4s-ember-server" % "0.23.23",
      "org.http4s"      %% "http4s-dsl"          % "0.23.23",
      "org.http4s"      %% "http4s-circe"        % "0.23.23",
      "org.typelevel"   %% "cats-effect"         % "3.5.2",
      "io.circe"        %% "circe-generic"       % "0.14.6"
    )
  )

lazy val root = (project in file("."))
  .aggregate(core, infrastructure)
  .settings(
    name := "scalaparty"
  )