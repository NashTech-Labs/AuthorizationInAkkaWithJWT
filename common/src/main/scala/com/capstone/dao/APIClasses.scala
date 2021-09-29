package com.capstone.dao

import java.time.OffsetDateTime
import java.util.UUID


final case class UserLoginDetails(userId: Option[String] = Some(
                                    UUID.randomUUID().toString),
                                  email: String,
                                  password: String,
                                  role: String,
                                  status: Option[Boolean],
                                  joinedDate: Option[String] = None,
                                  loginDate: Option[String] = None)



final case class AdminLoginRequest(email: String,
                                   name: String,
                                   password: String,
                                   role: String,
                                   key: Option[String])





