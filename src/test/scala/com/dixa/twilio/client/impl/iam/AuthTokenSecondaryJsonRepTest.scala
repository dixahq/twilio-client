package com.dixa.twilio.client.impl.iam
import com.dixa.twilio.client.TwilioTestConstants
import org.scalatest.wordspec.AnyWordSpec

final class AuthTokenSecondaryJsonRepTest extends AnyWordSpec {

  classOf[AuthTokenSecondaryJsonRep].getSimpleName should {

    "not print the actual token in its toString method" in {
      val i = AuthTokenSecondaryJsonRep(
        TwilioTestConstants.accountSid.toString,
        TwilioTestConstants.createdTime.toString,
        TwilioTestConstants.updatedTime.toString,
        "ThisIsVerySecret"
      )
      assert(!i.toString.contains("ThisIsVerySecret"))
    }
  }
}
