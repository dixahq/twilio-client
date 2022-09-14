package com.dixa.twilio.client.impl.iam

import com.dixa.twilio.client.TwilioTestConstants
import org.scalatest.wordspec.AnyWordSpec

final class AuthTokenCreateRespJsonRepTest extends AnyWordSpec {

  classOf[AuthTokenPrimaryJsonRep].getSimpleName should {

    "not print the actual token in it toString method" in {
      val i = AuthTokenPrimaryJsonRep(
        TwilioTestConstants.accountSid.toString,
        TwilioTestConstants.createdTime.toString,
        TwilioTestConstants.updatedTime.toString,
        "ThisIsVerySecret"
      )
      assert(!i.toString.contains("ThisIsVerySecret"))
    }
  }
}
