package com.capstone.admin.handler

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.testkit.TestActorRef
import com.capstone.admin.actor.AdminActor._
import com.capstone.components.AdminDAO
import com.capstone.dao.{AdminLoginRequest, UserLoginDetails}
import com.capstone.models.UserLoginRequest
import org.mockito.MockitoSugar
import org.scalatest.WordSpec

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class AdminServiceHandlerSpec extends WordSpec with AdminServiceHandler with MockitoSugar {

  implicit val adminDAO: AdminDAO = mock[AdminDAO]

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val email="email@email.com"


  val adminLoginRequest: AdminLoginRequest =AdminLoginRequest(email="email1@email.com",
    name="name",
    password="password",
    role=ADMIN,
    key=Some("admin key"))

  val userLoginRequest: UserLoginRequest = UserLoginRequest(userId=None,
    email=email,
    password="passowrd",
    role=ADMIN,
    name=Some("name"))


  "send UnprocessableEntity status when failed to create admin or user" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case _  ⇒
          sender ! Updated(true)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest.copy(role = "")), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Created status when admin succeed to create admin" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateAdminKey(_,_)  ⇒
          sender ! Validation(true)
        case ValidateAdminEmail(_)  ⇒
          sender ! Validation(false)
        case  CreateAdminOrUser(_)=>
          sender ! AdminAccountCreated(adminLoginRequest)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Conflict status when admin failed to create admin due to invalid input" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateAdminKey(_,_)  ⇒
          sender ! Validation(true)
        case ValidateAdminEmail(_)  ⇒
          sender ! Validation(false)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest.copy(name = "a")), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }
  "send Conflict status when admin failed to create admin" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateAdminKey(_,_)  ⇒
          sender ! Validation(true)
        case ValidateAdminEmail(_)  ⇒
          sender ! Validation(false)
        case  CreateAdminOrUser(_)=>
          sender ! Updated(false)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send Conflict status when admin or already exists" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateAdminKey(_,_)  ⇒
          sender ! Validation(true)
        case ValidateAdminEmail(_)  ⇒
          sender ! Validation(true)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Conflict status when admin tried to created admin with invalid key" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case ValidateAdminKey(_,_)  ⇒
          sender ! Validation(false)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Forbidden when admin failed to log in" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckAdminOrUserExists(_,_,_) ⇒
          sender ! ValidationResponse(false)

      }
    })

    val result = Await.result(loginRequest(command, userLoginRequest), 5 second)
    assert(result.status == StatusCodes.Forbidden)
  }

  "send Ok when valid admin send request to log in" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckAdminOrUserExists(_,_,_) ⇒
          sender ! ValidationResponse(true)
      }
    })

    val result = Await.result(loginRequest(command, userLoginRequest), 5 second)
    assert(result.status == StatusCodes.OK)
  }

  "send UnprocessableEntity when invalid email is entered" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckAdminOrUserExists(_,_,_) ⇒
          sender ! ValidationResponse(true)
      }
    })

    val result = Await.result(loginRequest(command, userLoginRequest.copy(email = "email")), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }

  "send Ok when user account is disabled" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfUserAccountEnabled(_,_) ⇒
          sender ! ValidationResponse(false)
        case IsValidPasswordRequest(_)=>
          sender() ! ValidationResponse(true)
      }
    })

    val result = Await.result(loginRequest(command, userLoginRequest.copy(role = USER)), 5 second)
    assert(result.status == StatusCodes.Unauthorized)
  }


  "send Ok when valid user send request to log in" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfUserAccountEnabled(_,_) ⇒
          sender ! ValidationResponse(true)
        case IsValidPasswordRequest(_)=>
          sender() ! ValidationResponse(true)
      }
    })

    val result = Await.result(loginRequest(command, userLoginRequest.copy(role = USER)), 5 second)
    assert(result.status == StatusCodes.OK)
  }

  "send Created status when admin succeed to create user" in {
    val userAccount=UserLoginDetails(userId = Some(
      UUID.randomUUID().toString),
      adminLoginRequest.email,
      adminLoginRequest.password,
      USER,
    status=Some(true),
    joinedDate=Some(OffsetDateTime.now().toString),None)
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfEmailExists(_)  ⇒
          sender ! Validation(false)
        case  CreateUser(_)=>
          sender ! UserAccountCreated(userAccount)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest.copy(role = USER)), 5 second)
    assert(result.status == StatusCodes.Created)
  }

  "send Conflict status when admin failed to create User due to invalid input" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfEmailExists(_)  ⇒
          sender ! Validation(false)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest
      .copy(role = USER,name = "n")), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }
  "send Conflict status when admin failed to create User" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfEmailExists(_)  ⇒
          sender ! Validation(false)
        case  CreateUser(_)=>
          sender ! Updated(false)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest.copy(role = USER)), 5 second)
    assert(result.status == StatusCodes.Conflict)
  }

  "send Conflict status when User already exists" in {
    val command = TestActorRef(new Actor {
      def receive: Receive = {
        case CheckIfEmailExists(_)  ⇒
          sender ! Validation(true)
      }
    })
    val result = Await.result(createAdminOrUser(command,email,adminLoginRequest.copy(role = USER)), 5 second)
    assert(result.status == StatusCodes.UnprocessableEntity)
  }


}