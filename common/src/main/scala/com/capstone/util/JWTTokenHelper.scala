package com.capstone.util

import java.time.Instant
import akka.http.scaladsl.server.directives.Credentials
import com.capstone.models._
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim, JwtHeader}
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
//do not remove the below import i.e import pureconfig.generic.auto._
import pureconfig.generic.auto._
import io.circe.parser
import io.circe.generic.auto._

trait JWTTokenHelper {

  val configs: Configurations = ConfigSource
    .resources("application.conf")
    .load[Configurations] match {
    case Left(e: ConfigReaderFailures) =>
      throw new RuntimeException(
        s"Unable to load AWS Config, original error: ${e.prettyPrint()}")
    case Right(x) => x
  }

  def setClaim(email: String, role: String): JwtClaim = {
    JwtClaim(
      expiration = Some(
        Instant.now
          .plusSeconds(configs.jwtScalaCirce.expireDurationSec)
          .getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      content = s"""{"email":"$email", "role":"$role"}"""
    )
  }

  def createJwtTokenWithRole(emailId: String, role: String): String = {
    JwtCirce.encode(setClaim(emailId, role),
                    configs.jwtScalaCirce.key,
                    JwtAlgorithm.HS256)
  }

  def decodeJwtTokenJson(token: String): Option[JWTTokenExtracts] = {
    JwtCirce
      .decodeJson(token, configs.jwtScalaCirce.key, Seq(JwtAlgorithm.HS256))
      .toOption match {
      case Some(json) =>
        parser.decode[JWTTokenExtracts](json.toString()).toOption
      case None =>
        throw new Exception("Not able to decode. Invalid JWT Token!!!")
    }
  }

  def validateToken(jwtToken: String): Boolean = {
    JwtCirce.isValid(jwtToken,
                     configs.jwtScalaCirce.key,
                     Seq(JwtAlgorithm.HS256))
  }

  /**
    * Get email id from token
    */
  def myUserPassAuthenticator(
      credentials: Credentials): Option[EmailAndRole] = {
    credentials match {
      case Credentials.Provided(id) if (validateToken(id)) =>
        Some(
          EmailAndRole(decodeJwtTokenJson(id).get.email,
                       decodeJwtTokenJson(id).get.role))
      case _ => None
    }
  }

}

object JWTTokenHelper extends JWTTokenHelper
