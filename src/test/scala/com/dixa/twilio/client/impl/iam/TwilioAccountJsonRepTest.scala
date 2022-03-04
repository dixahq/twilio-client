package com.dixa.twilio.client.impl.iam

import org.scalatest.wordspec.AnyWordSpec

final class TwilioAccountJsonRepTest extends AnyWordSpec {

  classOf[TwilioAccountJsonRep].getSimpleName should {

    "Should not expose the auth token in its toString, to ensure that it does not by " +
      "mistake ends up in a log somewhere" in {

        val authTokenString = "testAuthToken"
        val instance = TwilioAccountJsonRep(
          "testStatus",
          "Wed, 23 Feb 2022 17:13:40 +0000",
          authTokenString,
          "testFreindlyName",
          "testOwnerSid",
          "testSid",
          "Wed, 23 Feb 2022 17:13:40 +0000",
          "Full"
        )
        assert(!instance.toString.contains(authTokenString))
      }
  }
}
