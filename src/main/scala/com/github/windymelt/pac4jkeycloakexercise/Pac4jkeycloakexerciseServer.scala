package com.github.windymelt.pac4jkeycloakexercise

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.std.Dispatcher
import com.comcast.ip4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.pac4j.http4s.Http4sWebContext

object Pac4jkeycloakexerciseServer {
  lazy val run: IO[ExitCode] = Dispatcher
    .parallel[IO]
    .use { d =>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(
          Logger.httpApp(true, true)(
            ({
              val routes = new Pac4jkeycloakexerciseRoutes(
                Http4sWebContext.withDispatcherInstance(d)
              )
              routes.loginRoute
            }).orNotFound
          )
        )
        .build
        .useForever
    } >> IO.pure(ExitCode.Success)
}
