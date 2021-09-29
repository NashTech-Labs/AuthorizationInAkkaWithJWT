package com.capstone.util

import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpResponse,
  StatusCodes
}
import com.capstone.models.APIDataResponse
import com.capstone.models.DataResponsesModels._

import scala.concurrent.Future

/**
  * Utility for handling the response formats
  */
trait ResponseUtil extends Constants with JsonHelper {

  val resourceName: String = "http://localhost"

  def generateCommonResponse(status: Boolean,
                             error: Option[List[Error]],
                             data: Option[String] = None,
                             resource: Option[String] = Some(resourceName))
    : StandardResponseForString = {
    StandardResponseForString(resource, status, error, data)
  }

  def generateUserLoginResponse(
      username: String,
      accessToken: String): StandardResponseForUserLogin = {
    StandardResponseForUserLogin(username, accessToken)
  }

  def generateCommonResponseForCaseClass(status: Boolean,
                                         error: Option[List[Error]],
                                         data: Option[APIDataResponse] = None,
                                         resource: Option[String] = Some(
                                           resourceName))
    : StandardResponseForCaseClass = {
    StandardResponseForCaseClass(resource, status, error, data)
  }
  def unauthorizedRouteResponse: Future[HttpResponse] = {
    Future.successful(
      HttpResponse(StatusCodes.Unauthorized,
                   entity = HttpEntity.formatted("Unauthorized Access"))
    )
  }
}

object ResponseUtil extends ResponseUtil
