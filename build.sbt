val Http4sVersion = "0.23.18"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.6"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.windymelt",
    name := "pac4j-keycloak-exercise",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
      "org.fusesource.jansi" % "jansi" % "2.4.0",
      "org.scalameta" %% "svm-subs" % "20.2.0",
      "org.pac4j" %% "http4s-pac4j" % "4.1.0",
      "org.pac4j" % "pac4j-oidc" % "5.7.0",
      "org.slf4j" % "slf4j-api" % "2.0.7"
    ),
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework"),
    scalacOptions -= "-Xfatal-warnings"
  )
