package com.capstone.util

import org.scalatest.WordSpec

class ValidationUtilSpec extends WordSpec with ValidationUtil {

  "be able to send true for valid email format" in {
    assert(isValidEmail("jagdish.singh@knoldus.com"))
  }

  "be able to send false for invalid email format" in {
    assert(!isValidEmail("jagdish.singhknoldus.com"))
  }
  "be able to send false for empty email format" in {
    assert(!isValidEmail(" "))
  }

}
