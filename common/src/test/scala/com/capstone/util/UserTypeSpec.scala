package com.capstone.util

import com.capstone.util.ResponseUtil.USER
import org.scalatest.WordSpec

class UserTypeSpec extends WordSpec {

  "should validate user type" in {
    assert(UserType.isValidType(USER))
  }

}
