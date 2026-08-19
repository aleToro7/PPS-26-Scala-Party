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
  )

lazy val root = (project in file("."))
  .aggregate(core, infrastructure)
  .settings(
    name := "scalaparty"
  )