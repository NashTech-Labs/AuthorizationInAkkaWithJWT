package com.capstone.admin.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import com.capstone.admin.handler.AdminServiceHandler
import com.capstone.dao.{AdminLoginRequest}
import com.capstone.util.JWTTokenHelper.myUserPassAuthenticator
import com.capstone.util._
import akka.http.scaladsl.server.Directives._
import com.capstone.models.UserLoginRequest
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

/**
  * Service to handle the admin routes
  */
trait AdminService extends AdminServiceHandler with LazyLogging with Constants {
  //scalastyle:off

  //noinspection ScalaStyle
  private def getRoutesForUser(actor: ActorRef): Route =
    pathPrefix(ADMIN) {
      path("login") {
        pathEnd {
          (post & entity(as[UserLoginRequest])) { request =>
            logger.info(
              s"UserRestService: sending request for login:  $request")
            val response = loginRequest(actor, request)
            complete(response)
          }
        }
      } ~
        path("create-user") {
          pathEnd {
            (post & entity(as[AdminLoginRequest])) { request =>
              authenticateOAuth2(BEARER_AUTHENTICATION, myUserPassAuthenticator) {
                auth =>
                  if (auth.role == ADMIN) {
                    logger.info(
                      s"AdminService: sending request to create admin:  ${request}")
                    val response =
                      createAdminOrUser(actor, auth.email, request)
                    complete(response)
                  } else {
                    complete(unauthorizedRouteResponse)
                  }
              }
            }
          }
        }
    }

  def getUserRoutes(command: ActorRef): Route = getRoutesForUser(command)
}
