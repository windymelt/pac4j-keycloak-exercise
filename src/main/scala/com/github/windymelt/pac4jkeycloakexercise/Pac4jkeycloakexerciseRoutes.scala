package com.github.windymelt.pac4jkeycloakexercise

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import org.http4s.AuthedRoutes
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.ResponseCookie
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.server.Router
import org.http4s.syntax.all._
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.Clients
import org.pac4j.core.config
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.pac4j.http4s.CallbackService
import org.pac4j.http4s.DefaultHttpActionAdapter
import org.pac4j.http4s.Http4sCacheSessionStore
import org.pac4j.http4s.Http4sWebContext
import org.pac4j.http4s.LogoutService
import org.pac4j.http4s.SecurityFilterMiddleware
import org.pac4j.http4s.Session
import org.pac4j.http4s.SessionConfig
import org.pac4j.oidc.client.KeycloakOidcClient
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.KeycloakOidcConfiguration

import java.util.Optional
import scala.concurrent.duration.FiniteDuration

class Pac4jkeycloakexerciseRoutes[F[_] <: AnyRef: Sync](
    contextBuilder: (Request[F], config.Config) => Http4sWebContext[F]
) {
  val dsl = new Http4sDsl[F] {}
  import dsl._

  private val conf = new ConfigFactory[F]().build()
  val callbackService =
    new CallbackService[F](
      conf,
      contextBuilder,
      Some("/")
    )
  val localLogoutService = new LogoutService[F](
    conf,
    contextBuilder,
    Some("/?defaulturlafterlogout"),
    Some("/logout"),
    destroySession = true
  )

  def rootRoute: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root =>
        import org.http4s.headers.`Content-Type`
        import org.http4s.MediaType

        Ok("""<html>
<body>
root
<p><a href="/login/oidc">Login</a></p>
<p><a href="/safe">Safe zone</a></p>
<p><a href="/logout">Logout</a></p>
</body>
</html>
""".stripMargin).map(_.withContentType(`Content-Type`(MediaType.text.html)))

      case req @ GET -> Root / "callback"  => callbackService.callback(req)
      case req @ POST -> Root / "callback" => callbackService.callback(req)
      case req @ GET -> Root / "logout"    => localLogoutService.logout(req)
    }
  }

  private val sessionConfig = SessionConfig(
    cookieName = "session",
    mkCookie = ResponseCookie(_, _, path = Some("/")),
    secret = "This is a secret".getBytes.toList,
    maxAge = FiniteDuration(5, "minutes")
  )

  private val authedTrivial: AuthedRoutes[List[CommonProfile], F] =
    Kleisli(_ => OptionT.liftF(Found(Location(uri"/"))))

  def authorizedRoutes: AuthedRoutes[List[CommonProfile], F] = {
    AuthedRoutes.of { case GET -> Root as user =>
      Ok(s"authorized! ${user.head.getEmail()}")
    }
  }

  def loginRoute = Router(
    "/" -> (Session.sessionManagement[F](sessionConfig) _)(rootRoute),
    "/login/oidc" -> Session
      .sessionManagement[F](sessionConfig)
      .compose(
        SecurityFilterMiddleware
          .securityFilter[F](conf, contextBuilder, authorizers = None)
      )
      .apply(authedTrivial),
    "/safe" -> Session
      .sessionManagement[F](sessionConfig)
      .compose(
        SecurityFilterMiddleware
          .securityFilter[F](conf, contextBuilder, authorizers = None)
      )(authorizedRoutes)
  )

}

class ConfigFactory[F[_] <: AnyRef: Sync] extends config.ConfigFactory {
  override def build(parameters: AnyRef*): config.Config = {
    val _ = parameters
    val clients = new Clients("http://localhost:8080/callback", oidcClient())

    val conf = new config.Config(clients)
    conf.setHttpActionAdapter(new DefaultHttpActionAdapter[F])
    conf.setSessionStore(new Http4sCacheSessionStore[F]())
    // conf.setSessionStore(new Http4sCookieSessionStore[F] {})

    // conf.addAuthorizer(
    //   "admin",
    //   new RequireAnyRoleAuthorizer("admin")
    // )

    conf
  }

  def oidcClient(): OidcClient = {
    // val conf = new OidcConfiguration()
    // conf.setClientId("pac4j")
    // conf.setSecret("OIcNtRaNgVzCeNoEHPjEJhSJpxLv9zPH")
    // conf.setDiscoveryURI(
    //   "http://localhost:8090/realms/pac4j-realm/.well-known/openid-configuration"
    // )
    // conf.setUseNonce(true)
    // conf.setAllowUnsignedIdTokens(true)
    // conf.setClientAuthenticationMethod(
    //   ClientAuthenticationMethod.CLIENT_SECRET_POST
    // )

    // val cli = new OidcClient(conf)
    val conf = new KeycloakOidcConfiguration()
    conf.setBaseUri("http://localhost:8090/")
    conf.setRealm("pac4j-realm")
    conf.setClientId("pac4j")
    conf.setSecret("OIcNtRaNgVzCeNoEHPjEJhSJpxLv9zPH")
    conf.setUseNonce(true)
    conf.setClientAuthenticationMethod(
      ClientAuthenticationMethod.CLIENT_SECRET_POST
    )

    val cli = new KeycloakOidcClient(conf)
    val authGen = new AuthorizationGenerator {
      override def generate(
          context: WebContext,
          sessionStore: SessionStore,
          profile: UserProfile
      ): Optional[UserProfile] = {
        profile.addRole("admin") // ここでなぜかもらえないので手動でとりあえず与える
        Optional.of(profile)
      }
    }
    cli.setAuthorizationGenerator(authGen)
    cli
  }
}
