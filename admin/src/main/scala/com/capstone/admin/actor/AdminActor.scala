package com.capstone.admin.actor

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.pipe
import com.capstone.components.AdminDAO
import com.capstone.dao._
import com.capstone.models.{APIDataResponse, UserLoginRequest}
import com.capstone.util.{Constants, HashEncryption}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
class AdminActor(adminDAO: AdminDAO)(
    implicit futureAwaitDuration: FiniteDuration)
    extends Actor
    with ActorLogging
    with LazyLogging
    with HashEncryption
    with Constants {

  import AdminActor._

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    sender() ! Status.Failure(reason)
  }

  override def receive: Receive = {

    case ValidateAdminEmail(email: String) =>
      val res = checkIfAdminExists(email)
      res.pipeTo(sender())
    case CheckIfEmailExists(email: String) =>
      val res = checkIfEmailExists(email)
      res.pipeTo(sender())
    case CreateAdminOrUser(admin: AdminLoginRequest) =>
      val res = createAdmin(admin)
      res.pipeTo(sender())
    case CreateUser(user: UserLoginDetails) =>
      val res = createUser(user)
      res.pipeTo(sender())
    case ValidateAdminKey(email: String, key: Option[String]) =>
      val res = validateAdminKey(email, key)
      res.pipeTo(sender())
    case IsValidPasswordRequest(loginRequest: UserLoginRequest) =>
      log.info("login request: " + loginRequest)
      val res =
        adminDAO
          .validateUser(loginRequest)
          .map(count => ValidationResponse(count > 0))
      res.pipeTo(sender())
    case CheckAdminOrUserExists(emailId: String,
                                password: String,
                                userType: String) =>
      val res = adminDAO
        .isAdminEmailExists(emailId, password, userType)
        .map(count => {
          ValidationResponse(count > 0)
        })
      res.pipeTo(sender())

    case CheckIfUserAccountEnabled(emailId: String, userType: String) =>
      val res = adminDAO
        .isUserAccountEnabled(emailId, userType)
        .map(count => {
          ValidationResponse(count > 0)
        })
      res.pipeTo(sender())
  }

  def checkIfAdminExists(email: String): Future[Validation] = {
    adminDAO.checkAdminEmail(email).map {
      case 0 =>
        logger.info("admin/user not exits")
        Validation(false)
      case 1 =>
        logger.info("admin/user exits")
        Validation(true)
    }
  }

  def checkIfEmailExists(email: String): Future[Validation] = {
    adminDAO.checkEmailExistsForUser(email).map {
      case 0 =>
        logger.info("admin/user not exits")
        Validation(false)
      case 1 =>
        logger.info("admin/user exits")
        Validation(true)
    }
  }

  def createAdmin(admin: AdminLoginRequest): Future[APIDataResponse] = {
    val encrypted = admin.copy(password = encrypt(admin.password),
                               key = Some(encrypt(admin.key.getOrElse(""))))
    adminDAO.createAdmin(encrypted).map {
      case 0 =>
        Updated(false)
      case 1 =>
        AdminAccountCreated(admin)
    }
  }

  def createUser(user: UserLoginDetails): Future[APIDataResponse] = {
    val encrypted = user.copy(password = encrypt(user.password))
    adminDAO.createUser(encrypted).map {
      case 0 =>
        Updated(false)
      case 1 =>
        UserAccountCreated(user)
    }
  }

  def validateAdminKey(email: String,
                       key: Option[String]): Future[Validation] = {
    log.info(s"going to validate admin key: $key for $email")
    adminDAO.validateAdminKey(email, key).map {
      case 0 =>
        Validation(false)
      case 1 =>
        Validation(true)
    }
  }

}

object AdminActor {
  // commands
  sealed trait AdminActorMessage

  final case class ValidateAdminEmail(email: String) extends AdminActorMessage
  final case class CheckIfEmailExists(email: String) extends AdminActorMessage
  final case class ValidateAdminKey(email: String, key: Option[String])
      extends AdminActorMessage
  final case class CheckAdminOrUserExists(emailId: String,
                                          password: String,
                                          userType: String)
      extends AdminActorMessage

  final case class IsValidPasswordRequest(loginRequest: UserLoginRequest)
      extends AdminActorMessage
  final case class CheckIfUserAccountEnabled(emailId: String, userType: String)
      extends AdminActorMessage

  final case class CreateAdminOrUser(admin: AdminLoginRequest)
      extends AdminActorMessage
  final case class CreateUser(user: UserLoginDetails) extends AdminActorMessage

  final case class Validation(isValid: Boolean) extends APIDataResponse
  final case class ValidationResponse(value: Boolean) extends APIDataResponse
  final case class Updated(status: Boolean) extends APIDataResponse
  final case class AdminAccountCreated(admin: AdminLoginRequest)
      extends APIDataResponse
  final case class UserAccountCreated(user: UserLoginDetails)
      extends APIDataResponse
  final case class NoDataFound() extends APIDataResponse

  def props(adminDAO: AdminDAO)(
      implicit futureAwaitDuration: FiniteDuration): Props =
    Props(new AdminActor(adminDAO))
}
