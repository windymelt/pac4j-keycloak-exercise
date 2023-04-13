package com.github.windymelt.pac4jkeycloakexercise

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = Pac4jkeycloakexerciseServer.run[IO]
}
