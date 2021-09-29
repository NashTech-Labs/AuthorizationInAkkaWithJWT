package com.capstone.admin.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.capstone.admin.actor.AdminActor._
import com.capstone.components.AdminDAO
import com.capstone.dao._
import com.capstone.util.JsonHelper
import com.capstone.util.ResponseUtil._
import org.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import slick.jdbc.H2Profile

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
class AdminActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with MustMatchers with BeforeAndAfterAll with JsonHelper with MockitoSugar {

  def this() = this(ActorSystem("adminActor"))

  val driver = H2Profile

  import driver.api.Database

  implicit val db: driver.api.Database = mock[Database]
  implicit val schema: String = ""
  val futureAwaitTime: FiniteDuration = 10.minute

  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)
  val adminDAO: AdminDAO = mock[AdminDAO]

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val email="email@email.com"

  val adminLoginRequest: AdminLoginRequest =AdminLoginRequest(email,
    "name",
    "password",
    role=ADMIN,
    key=None)
  val user=UserLoginDetails(userId = Some(
    UUID.randomUUID().toString),
    adminLoginRequest.email,
    adminLoginRequest.password,
    USER,
    status=Some(true),
    joinedDate=Some(OffsetDateTime.now().toString),None)

  "A AdminActor" must {


    "be able to ValidateAdminOrUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.checkAdminEmail(email)) thenReturn Future(1)
      }))
      actorRef ! ValidateAdminEmail(email)
      expectMsgType[Validation](5 seconds)
    }

    "not be able to ValidateAdminOrUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.checkAdminEmail(email)) thenReturn Future(0)
      }))
      actorRef ! ValidateAdminEmail(email)
      expectMsgType[Validation](5 seconds)
    }


    "be able to ValidateAdminKey" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.validateAdminKey(email,Some("key"))) thenReturn Future(1)
      }))
      actorRef ! ValidateAdminKey(email,Some("key"))
      expectMsgType[Validation](5 seconds)
    }

    "not be able to ValidateAdminKey" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.validateAdminKey(email,Some("key"))) thenReturn Future(0)
      }))
      actorRef ! ValidateAdminKey(email,Some("key"))
      expectMsgType[Validation](5 seconds)
    }


    "be able to CreateAdminOrUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.createAdmin(adminLoginRequest.copy(password = "P9gjX7Gg6eSD560Hsa5+4Q==",
          key = Some("DdDfND8tluGfRwpmNNUW/w==")))) thenReturn Future(1)
      }))
      actorRef ! CreateAdminOrUser(adminLoginRequest)
      expectMsgType[AdminAccountCreated](5 seconds)
    }

    "not be able to CreateAdminOrUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.createAdmin(adminLoginRequest.copy(password = "P9gjX7Gg6eSD560Hsa5+4Q==",
          key = Some("DdDfND8tluGfRwpmNNUW/w==")))) thenReturn Future(0)
      }))
      actorRef ! CreateAdminOrUser(adminLoginRequest)
      expectMsgType[Updated](5 seconds)
    }



    "be able to CheckIfEmailExists" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.checkEmailExistsForUser(email)) thenReturn Future(1)
      }))
      actorRef ! CheckIfEmailExists(email)
      expectMsgType[Validation](5 seconds)
    }

    "not be able to CheckIfEmailExists" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.checkEmailExistsForUser(email)) thenReturn Future(0)
      }))
      actorRef ! CheckIfEmailExists(email)
      expectMsgType[Validation](5 seconds)
    }

    "be able to CreateUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.createUser(user.copy(password = encrypt(user.password)))) thenReturn
          Future(1)
      }))
      actorRef ! CreateUser(user)
      expectMsgType[UserAccountCreated](5 seconds)
    }

    "not be able to CreateUser" in {
      val actorRef = system.actorOf(Props(new AdminActor(adminDAO) {
        when(adminDAO.createUser(user.copy(password = encrypt(user.password)))) thenReturn
          Future(0)
      }))
      actorRef ! CreateUser(user)
      expectMsgType[Updated](5 seconds)
    }
  }
}