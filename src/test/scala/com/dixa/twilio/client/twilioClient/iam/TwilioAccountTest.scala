package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.model.iam.TwilioAccount
import org.scalatest.wordspec.AnyWordSpec

final class TwilioAccountTest extends AnyWordSpec {

  classOf[TwilioAccount.AuthToken].getSimpleName should {

    "replace it's actual value with *** in toString, so that we do not accidentally log " +
      "or prints auth tokens where we should not" in {

        val instance = TwilioAccount.AuthToken("aVerySecretValue")
        assert(instance.toString === "AuthToken(***)")
      }
  }
}
