package com.capstone.persistence

import com.capstone.components.AdminDAO
import com.capstone.dao.{AdminLoginRequest, UserLoginDetails}
import com.capstone.models.UserLoginRequest
import com.capstone.util.ResponseUtil._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AsyncWordSpecLike, Matchers}

import java.util.UUID

class AdminDAOSpec extends AsyncWordSpecLike with ScalaFutures with Matchers with ConfigLoader {

  implicit val defaultPatience: PatienceConfig =
      PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  implicit val searchLimit: Int = 2
  val adminDAO = new AdminDAO()

  val userId: String =UUID.randomUUID().toString
  val password="password"

  val userLogin: UserLoginDetails = UserLoginDetails(
    userId = Some(userId),
    email = "email@email.com",
    password = password,
    role = USER,
    status = Some(true))

  val userLoginRequest =UserLoginRequest(email="", password="", role=USER,name=None)


  val admin: AdminLoginRequest =AdminLoginRequest(email="email1@email.com",
    name="name",
    password=password,
    role=ADMIN,
    key=Some("admin key"))

  "AdminDAOSpec service" should {

    "be able to create admin" in{
      whenReady(adminDAO.createAdmin(admin)) { res=>
        res shouldBe 1
      }
    }

    "be able to create user" in{
      whenReady(adminDAO.createUser(userLogin)) { res=>
        res shouldBe 1
      }
    }

    "be able to check admin or user" in{
      whenReady(adminDAO.checkAdminEmail("email2@email.com")) { res=>
        res shouldBe 0
      }
    }

    "be able to validate admin key" in{
      whenReady(adminDAO.validateAdminKey("email4@email.com",admin.key)) { res=>
        res shouldBe 0
      }
    }

    "be able to check if email exist to create user" in{
      whenReady(adminDAO.checkEmailExistsForUser(userLogin.email)) { res=>
        res shouldBe 1
      }
    }

    "be able to check if account is enabled" in{
      whenReady(adminDAO.isUserAccountEnabled(admin.email,admin.role)) { res=>
        res shouldBe 0
      }
    }

    "be able to check if admin account exits" in{
      whenReady(adminDAO.isAdminEmailExists(admin.email,admin.password,admin.role)) { res=>
        res shouldBe 1
      }
    }

    "be able to validate user" in{
      whenReady(adminDAO.validateUser(userLoginRequest)) { res=>
        res shouldBe 0
      }
    }

  }
}
