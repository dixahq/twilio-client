package com.dixa.twilio.client.impl.iam
import com.dixa.twilio.client.TwilioTestConstants
import org.scalatest.wordspec.AnyWordSpec

final class SecondaryAuthTokenCreateRespJsonRepTest extends AnyWordSpec {

  classOf[SecondaryAuthTokenCreateRespJsonRep].getSimpleName should {

    "not print the actual token in it's toString method" in {
      val i = SecondaryAuthTokenCreateRespJsonRep(
        TwilioTestConstants.accountSid.toString,
        TwilioTestConstants.createdTime.toString,
        TwilioTestConstants.updatedTime.toString,
        "ThisIsVerySecret"
      )
      assert(!i.toString.contains("ThisIsVerySecret"))
    }
  }
}
