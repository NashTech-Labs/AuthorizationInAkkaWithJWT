package com.capstone.util

import org.scalatest.WordSpec

class JWTTokenHelperSpec extends WordSpec with JWTTokenHelper {
  "should be able to create a Valid JWT token" in {
    val token = createJwtTokenWithRole("jagdish.singh@knoldus.com", "admin")
    val decodedToken = decodeJwtTokenJson(token)
    assert(decodedToken.get.email.equals("jagdish.singh@knoldus.com"))
    assert(decodedToken.get.role.equals("admin"))
  }

  "should check if the provided token is Valid" in  {
    val token = createJwtTokenWithRole("jagdish.singh@knoldus.com", "admin")
    assert(validateToken(token))
  }

  "should check if the provided token is NOT Valid" in  {
    val token = "some_invalid_token.this_is_a_invalid_token"
    assert(!validateToken(token))
  }
}
