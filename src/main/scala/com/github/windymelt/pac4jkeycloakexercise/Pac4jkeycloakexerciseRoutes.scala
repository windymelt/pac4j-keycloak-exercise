package com.github.windymelt.pac4jkeycloakexercise

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object Pac4jkeycloakexerciseRoutes {

  def rootRoute[F[_]: Sync]: HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root =>
      import org.http4s.headers.`Content-Type`
      import org.http4s.MediaType

      Ok("""<html>
<body>
root
</body>
</html>
""".stripMargin).map(_.withContentType(`Content-Type`(MediaType.text.html)))

    }
  }
}
