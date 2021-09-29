package com.capstone.components

import com.capstone.dao._
import com.capstone.models._
import com.capstone.util._
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

class AdminDAO(implicit val db: Database,
               schema: String,
               ec: ExecutionContext,
               searchLimit: Int)
    extends LazyLogging
    with Constants {

  val userLoginQuery = TableQuery[UserLoginTable]
  val userAdminQuery = TableQuery[UserAdminTable]

  def createAdmin(admin: AdminLoginRequest): Future[Int] = {
    db.run(userAdminQuery += admin)
  }

  def createUser(user: UserLoginDetails): Future[Int] = {
    db.run(userLoginQuery += user)
  }

  def isUserAccountEnabled(email: String, userType: String): Future[Int] = {
    db.run(
      userLoginQuery
        .filter(col =>
          col.email === email && col.role === userType && col.status === true)
        .size
        .result)
  }

  def isAdminEmailExists(email: String,
                         password: String,
                         userType: String): Future[Int] = {
    db.run(
      userAdminQuery
        .filter(
          c =>
            c.email === email &&
              c.role === userType &&
              c.password === password)
        .size
        .result)
  }

  def validateUser(login: UserLoginRequest): Future[Int] = {
    db.run(
      userLoginQuery
        .filter(
          col =>
            col.email === login.email &&
              col.password === login.password &&
              col.role === login.role)
        .size
        .result)
  }

  def checkAdminEmail(email: String): Future[Int] = {
    val query = userAdminQuery
      .filter(col => col.email === email)
      .size
      .result
    db.run(query)
  }

  def checkEmailExistsForUser(email: String): Future[Int] = {
    val query = userLoginQuery
      .filter(col => col.email === email)
      .size
      .result
    db.run(query)
  }

  def validateAdminKey(email: String, key: Option[String]): Future[Int] = {
    val query = userAdminQuery
      .filter(col => col.email === email && col.key === key)
      .size
      .result
    db.run(query)
  }

}
