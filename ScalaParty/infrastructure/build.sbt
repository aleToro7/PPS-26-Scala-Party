scalaVersion := "3.8.4"

lazy val root = rootProject
  .settings(
    name := "infrastructure",
    idePackagePrefix := Some("com.scalaparty.infrastructure"),
    libraryDependencies ++= Seq(
      //You can add library dependencies here, for example,
      //"org.scalatest" %% "scalatest" % "3.2.19" % Test,
      //"org.scalameta" %% "munit" % "1.2.3" % Test
    )
  )
