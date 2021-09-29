package com.capstone.admin.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.capstone.admin.actor.AdminActor
import com.capstone.admin.handler.AdminServiceHandler
import com.capstone.components.AdminDAO
import com.capstone.dao.AdminLoginRequest
import com.capstone.models.UserLoginRequest
import com.capstone.util.JWTTokenHelper.createJwtTokenWithRole
import org.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import slick.jdbc.H2Profile

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}

class AdminServiceSpec extends WordSpec with Matchers with ScalatestRouteTest
  with AdminService with MockitoSugar with AdminServiceHandler {

  val driver = H2Profile

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""

  val adminDAO: AdminDAO = mock[AdminDAO]

  val futureAwaitTime: FiniteDuration = 10.minute

  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val adminActor: ActorRef = system.actorOf(
    AdminActor
      .props(adminDAO),
    "adminActor")

  val route: Route = getUserRoutes(adminActor)

  val adminToken:String = createJwtTokenWithRole("admin@gmail.com", "admin")
  val adminAuth: Authorization = Authorization(OAuth2BearerToken(adminToken))
  val userToken:String = createJwtTokenWithRole("user@gmail.com", "user")
  val userAuth: Authorization = Authorization(OAuth2BearerToken(userToken))


  override def createAdmin(command: ActorRef,
                           email: String,
                           admin: AdminLoginRequest
                          ): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  override def loginRequest(command: ActorRef,
                            loginRequest: UserLoginRequest): Future[HttpResponse] = Future.successful(HttpResponse.apply())

  "return OK when user logged in" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""{
         |    "email": "email@mail.com",
         |    "password": "password",
         |    "role": "user"
         |}""".stripMargin)
    Post("/admin/login", data) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

    "return ok when create admin route is hit by admin" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""{
         |    "name": "admin admin",
         |    "email": "admin@gmail.com",
         |    "password": "admin@admin!123",
         |    "role": "admin"
         |}""".stripMargin)
    Post("/admin/create-user", data).addHeader(adminAuth) ~> route ~> check {
      status shouldBe StatusCodes.OK
    }
  }

  "return Unauthorized when create admin route is hit by Unauthorized user" in {
    val data = HttpEntity(
      ContentTypes.`application/json`,
      s"""{
         |    "name": "admin admin",
         |    "email": "admin@gmail.com",
         |    "password": "admin@admin!123",
         |    "role": "admin"
         |}""".stripMargin)
    Post("/admin/create-user", data).addHeader(userAuth) ~> route ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }



}