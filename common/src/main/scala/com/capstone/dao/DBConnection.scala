package com.capstone.dao

import slick.jdbc.MySQLProfile.api._
import slick.lifted.{PrimaryKey, ProvenShape}

import java.time.OffsetDateTime

final class UserLoginTable(tag: Tag)(implicit val schema: String)
    extends Table[UserLoginDetails](tag, Some(schema), "user_login") {
  def userId: Rep[Option[String]] = column[Option[String]]("userid")
  def email: Rep[String] = column[String]("email")
  def password: Rep[String] = column[String]("password")
  def joinedDate: Rep[Option[String]] =
    column[Option[String]]("created_date")
  def lastLogin: Rep[Option[String]] =
    column[Option[String]]("last_login")
  def role: Rep[String] = column[String]("user_type")
  def status: Rep[Option[Boolean]] = column[Option[Boolean]]("status")

  //noinspection ScalaStyle
  def * : ProvenShape[UserLoginDetails] =
    (userId, email, password, role, status, joinedDate, lastLogin).shaped <> (UserLoginDetails.tupled, UserLoginDetails.unapply)

  implicit def primary: (Rep[String], Rep[String]) =
    (email, role)
  def pk: PrimaryKey = primaryKey("pk_a", (email, role))
}

final class UserAdminTable(tag: Tag)(implicit val schema: String)
    extends Table[AdminLoginRequest](tag, Some(schema), "user_admin") {
  def email: Rep[String] = column[String]("email", O.PrimaryKey)
  def name: Rep[String] = column[String]("name")
  def password: Rep[String] = column[String]("password")
  def role: Rep[String] = column[String]("user_type")
  def key: Rep[Option[String]] = column[Option[String]]("key_to_create")

  //noinspection ScalaStyle
  def * : ProvenShape[AdminLoginRequest] =
    (email, name, password, role, key).shaped <> (AdminLoginRequest.tupled, AdminLoginRequest.unapply)

}

