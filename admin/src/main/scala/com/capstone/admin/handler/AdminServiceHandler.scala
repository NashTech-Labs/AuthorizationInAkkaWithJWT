package com.capstone.admin.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.Timeout
import com.capstone.admin.actor.AdminActor._
import com.capstone.dao._
import com.capstone.models.UserLoginRequest
import com.capstone.util.JWTTokenHelper.createJwtTokenWithRole
import com.capstone.util._
import com.typesafe.scalalogging.LazyLogging

import java.time.OffsetDateTime
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
trait AdminServiceHandler
    extends ResponseUtil
    with JsonHelper
    with ValidationUtil
    with HashEncryption
    with LazyLogging
    with Constants {

  implicit val system: ActorSystem

  implicit val materializer: Materializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  import akka.pattern.ask
  import system.dispatcher

  def loginRequest(command: ActorRef,
                   loginRequest: UserLoginRequest): Future[HttpResponse] = {
    if (loginRequest.role.toLowerCase() == ADMIN) {
      loginAdminOrUser(command, loginRequest)
    } else if (loginRequest.role.toLowerCase() == USER) {
      loginUser(command, loginRequest)
    } else {
      Future.successful(
        HttpResponse(
          StatusCodes.UnprocessableEntity,
          entity = HttpEntity(ContentTypes.`application/json`,
                              write(
                                generateCommonResponse(status = false,
                                                       Some(List()),
                                                       Some(INVALID_USER_TYPE),
                                                       None)))
        ))
    }
  }

  def loginUser(command: ActorRef,
                loginRequest: UserLoginRequest): Future[HttpResponse] = {
    if (isValidEmail(loginRequest.email)) {
      ask(command,
        CheckIfUserAccountEnabled(loginRequest.email, loginRequest.role))
        .flatMap {
          case ValidationResponse(true) =>
            ask(command,
                IsValidPasswordRequest(
                  loginRequest.copy(password = encrypt(loginRequest.password))))
              .flatMap {
                //email id and password is valid
                case ValidationResponse(true) =>
                  loginSuccessResponse(command, loginRequest)
                // user email and password  is invalid
                case ValidationResponse(false) =>
                  Future.successful(
                    HttpResponse(
                      StatusCodes.Conflict,
                      entity = HttpEntity(
                        ContentTypes.`application/json`,
                        write(
                          generateCommonResponse(status = false,
                                                 Some(List()),
                                                 Some(PASSWORD_NOT_MATCHED),
                                                 None)))
                    ))
              }

          case ValidationResponse(false) =>
            Future.successful(
              HttpResponse(
                StatusCodes.Unauthorized,
                entity = HttpEntity(ContentTypes.`application/json`,
                                    write(
                                      generateCommonResponse(
                                        status = false,
                                        Some(List()),
                                        Some(INVALID_EMAIL_OR_ACCOUNT_DETAILS),
                                        None)))
              ))
        }
    } else {
      Future.successful(
        HttpResponse(
          StatusCodes.UnprocessableEntity,
          entity = HttpEntity(ContentTypes.`application/json`,
                              write(
                                generateCommonResponse(status = false,
                                                       Some(List()),
                                                       Some(INVALID_EMAIL_ID),
                                                       None)))
        ))
    }
  }

  def loginSuccessResponse(
      command: ActorRef,
      loginRequest: UserLoginRequest): Future[HttpResponse] = {
    val token =
      createJwtTokenWithRole(loginRequest.email, loginRequest.role)

    Future.successful(
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`application/json`,
          write(generateUserLoginResponse(username = loginRequest.email, token))
        )
      ))
  }

  def loginAdminOrUser(
      command: ActorRef,
      loginRequest: UserLoginRequest): Future[HttpResponse] = {
    logger.info(s"going to validate details for login $loginRequest")
    if (isValidEmail(loginRequest.email)) {
      ask(command,
          CheckAdminOrUserExists(loginRequest.email,
                                             encrypt(loginRequest.password),
                                             loginRequest.role))
        .flatMap {
          case ValidationResponse(true) =>
            logger.info(s"user is validated ${loginRequest.email}, ${encrypt(loginRequest.password)}")
            loginSuccessResponse(command, loginRequest)
          case ValidationResponse(false) =>
            logger.info(s"user invalid ${loginRequest.email}, ${encrypt(loginRequest.password)}")
            Future.successful(
              HttpResponse(
                StatusCodes.Forbidden,
                entity =
                  HttpEntity(ContentTypes.`application/json`,
                             write(
                               generateCommonResponse(status = false,
                                                      Some(List()),
                                                      Some(INVALID_CREDENTIALS),
                                                      None)))
              ))
        }
    } else {

      Future.successful(
        HttpResponse(
          StatusCodes.UnprocessableEntity,
          entity = HttpEntity(ContentTypes.`application/json`,
                              write(
                                generateCommonResponse(status = false,
                                                       Some(List()),
                                                       Some(INVALID_EMAIL_ID),
                                                       None)))
        ))
    }
  }
  def createAdminOrUser(command: ActorRef,
                        email: String,
                        admin: AdminLoginRequest): Future[HttpResponse] = {
    if (admin.role.toLowerCase() == ADMIN) {
      createAdmin(command, email, admin)
    } else if (admin.role.toLowerCase() == USER) {
      createUser(command, admin)
    } else {
      Future.successful(
        HttpResponse(
          StatusCodes.UnprocessableEntity,
          entity = HttpEntity(ContentTypes.`application/json`,
                              write(
                                generateCommonResponse(status = false,
                                                       Some(List()),
                                                       Some(INVALID_USER_TYPE),
                                                       None)))
        ))
    }
  }

  def createAdmin(command: ActorRef,
                  adminEmail: String,
                  admin: AdminLoginRequest): Future[HttpResponse] = {
    logger.info(s"going to check if admin exists $admin")
    ask(command,
        ValidateAdminKey(adminEmail, Some(encrypt(admin.key.getOrElse("")))))
      .flatMap {
        case Validation(true) =>
          ask(command, ValidateAdminEmail(admin.email)).flatMap {
            case Validation(false) =>
              logger.info(s"going to create admin $admin")
              if (admin.name.length > TWO && isValidEmail(admin.email)) {
                val generatedKey =
                  Some(
                    admin.name
                      .take(TWO) + UUID.randomUUID().toString.take(EIGHT))
                ask(
                  command,
                  CreateAdminOrUser(admin.copy(key = generatedKey))).map {
                  case Updated(false) =>
                    HttpResponse(
                      StatusCodes.Conflict,
                      entity =
                        HttpEntity(ContentTypes.`application/json`,
                                   write(
                                     generateCommonResponse(status = false,
                                                            Some(List()),
                                                            Some(INVALID_INPUT),
                                                            None)))
                    )
                  case response: AdminAccountCreated =>
                    HttpResponse(
                      StatusCodes.Created,
                      entity = HttpEntity(
                        ContentTypes.`application/json`,
                        write(
                          generateCommonResponseForCaseClass(status = true,
                                                             Some(List()),
                                                             Some(response),
                                                             None)))
                    )
                }
              } else {

                logger.info(s"Invalid input name must have 2 alphabets")
                Future.successful(
                  HttpResponse(
                    StatusCodes.Conflict,
                    entity = HttpEntity(
                      ContentTypes.`application/json`,
                      write(
                        generateCommonResponse(status = false,
                                               Some(List()),
                                               Some("INVALID_NAME_OR_EMAIL"),
                                               None)))
                  ))
              }
            case Validation(true) =>
              logger.info(s"admin already exists $admin")
              Future.successful(
                HttpResponse(
                  StatusCodes.UnprocessableEntity,
                  entity = HttpEntity(
                    ContentTypes.`application/json`,
                    write(
                      generateCommonResponse(status = false,
                                             Some(List()),
                                             Some(EMAIl_ALREADY_EXISTS),
                                             None)))
                ))
          }
        case Validation(false) =>
          logger.info(s"Invalid admin key ${admin.key} for $adminEmail")
          Future.successful(
            HttpResponse(
              StatusCodes.UnprocessableEntity,
              entity =
                HttpEntity(ContentTypes.`application/json`,
                           write(
                             generateCommonResponse(status = false,
                                                    Some(List()),
                                                    Some(INVALID_ADMIN_KEY),
                                                    None)))
            ))
      }
  }

  def createUser(command: ActorRef,
                 request: AdminLoginRequest): Future[HttpResponse] = {
    logger.info(s"going to check if user exists $request")
    ask(command, CheckIfEmailExists(request.email)).flatMap {
      case Validation(false) =>
        logger.info(s"going to create user $request")
        if (request.name.length > 2 && isValidEmail(request.email)) {
          val userDetails =
            UserLoginDetails(userId = Some(UUID.randomUUID().toString),
                             request.email,
                             request.password,
                             request.role,
                             status = Some(true),
                             joinedDate = Some(OffsetDateTime.now().toString))
          ask(command, CreateUser(userDetails)).map {
            case Updated(false) =>
              HttpResponse(
                StatusCodes.Conflict,
                entity =
                  HttpEntity(ContentTypes.`application/json`,
                             write(
                               generateCommonResponse(status = true,
                                                      Some(List()),
                                                      Some(INVALID_INPUT),
                                                      None)))
              )
            case response: UserAccountCreated =>
              HttpResponse(
                StatusCodes.Created,
                entity = HttpEntity(
                  ContentTypes.`application/json`,
                  write(
                    generateCommonResponseForCaseClass(status = true,
                                                       Some(List()),
                                                       Some(response),
                                                       None)))
              )
          }
        } else {

          Future.successful(
            HttpResponse(
              StatusCodes.Conflict,
              entity = HttpEntity(ContentTypes.`application/json`,
                                  write(
                                    generateCommonResponse(status = false,
                                                           Some(List()),
                                                           Some(INVALID_INPUT),
                                                           None)))
            ))
        }
      case Validation(true) =>
        logger.info(s"email already exists $request")
        Future.successful(
          HttpResponse(
            StatusCodes.UnprocessableEntity,
            entity =
              HttpEntity(ContentTypes.`application/json`,
                         write(
                           generateCommonResponse(status = false,
                                                  Some(List()),
                                                  Some(EMAIl_ALREADY_EXISTS),
                                                  None)))
          ))
    }
  }

}
