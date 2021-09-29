package com.capstone.models

import java.util.UUID

case class UserLoginRequest(userId: Option[String] = Some(
                              UUID.randomUUID().toString),
                            email: String,
                            password: String,
                            role: String,
                            name: Option[String])

final case class EmailAndRole(email: String, role: String)

case class JWTTokenExtracts(exp: Long, iat: Long, email: String, role: String)
