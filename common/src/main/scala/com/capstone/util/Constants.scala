package com.capstone.util

import akka.http.scaladsl.model.{
  HttpEntity,
  HttpProtocols,
  HttpResponse,
  StatusCodes
}

trait Constants {

  // magic numbers
  val TWO = 2
  val EIGHT = 8

  val BEARER_AUTHENTICATION = "Bearer Authentication"
  val NO_DATA_RESPONSE: HttpResponse = HttpResponse(StatusCodes.NoContent,
                                                    Nil,
                                                    HttpEntity.Empty,
                                                    HttpProtocols.`HTTP/1.1`)

  val ADMIN = "admin"
  val INVALID_EMAIL_ID = "Invalid Email Id"
  val INVALID_EMAIL_OR_ACCOUNT_DETAILS = "INVALID EMAIL OR ACCOUNT DETAILS"

  val USER_ALREADY_EXIST = "USER_ALREADY_EXIST"
  val EMAIl_ALREADY_EXISTS = "EMAIl_ALREADY_EXISTS"
  val INVALID_INPUT = "INVALID_INPUT"
  val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
  val INVALID_USER_TYPE = "INVALID_USER_TYPE"
  val INVALID_ADMIN_KEY = "INVALID_ADMIN_KEY"
  val PASSWORD_NOT_MATCHED = "Password not Matched"
  val USER = "user"

}
