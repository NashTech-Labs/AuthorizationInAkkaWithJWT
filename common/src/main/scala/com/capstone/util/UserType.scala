package com.capstone.util

object UserType extends Enumeration {
  type Status = Value
  val PREMIUM_USER = Value("premium")
  val USER = Value("user")

  def isValidType(s: String): Boolean = values.exists(_.toString == s)
}
