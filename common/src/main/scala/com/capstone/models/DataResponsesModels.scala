package com.capstone.models

import java.time.OffsetDateTime

trait APIDataResponse

object DataResponsesModels {

  case class StandardResponseForString(
      resource: Option[String],
      status: Boolean,
      errors: Option[List[Error]],
      data: Option[String]
  )

  case class StandardResponseForUserLogin(username: String, accessToken: String)

  case class StandardResponseForCaseClass(
      resource: Option[String],
      status: Boolean,
      errors: Option[List[Error]],
      data: Option[APIDataResponse]
  )

}
