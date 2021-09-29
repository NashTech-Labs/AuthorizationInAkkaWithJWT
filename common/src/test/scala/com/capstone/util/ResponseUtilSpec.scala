package com.capstone.util

import akka.http.scaladsl.model.StatusCodes
import com.capstone.models.DataResponsesModels._
import org.scalatest.WordSpec

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

class ResponseUtilSpec extends WordSpec with ResponseUtil {

  "ResponseUtilSpec in common module" should {
    "generate a commonResponse" in {
      val res = generateCommonResponse(status = true, None, Some("registered"), Some("REGISTER"))
      assert(res == StandardResponseForString(Some("REGISTER"), status = true, None, Some("registered")))
    }

    "generate a commonResponseForCaseClass" in {
      val res = generateCommonResponseForCaseClass(status = true, None, None, Some("REGISTER"))
      assert(res == StandardResponseForCaseClass(Some("REGISTER"), status = true, None, None))
    }
    "generate a generateUserLoginResponse" in {
      val res = generateUserLoginResponse("username", "token")
      assert(res == StandardResponseForUserLogin("username", "token"))
    }

    "return unauthorized response" in {
      val res = Await.result(unauthorizedRouteResponse, 5 second)
      assert(res.status==StatusCodes.Unauthorized)
    }

  }
}
