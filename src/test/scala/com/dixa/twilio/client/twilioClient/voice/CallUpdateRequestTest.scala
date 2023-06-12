package com.dixa.twilio.client.twilioClient.voice

import org.scalatest.wordspec.AnyWordSpec

// The test results are cached so may need to run sbt clean when test code is changed

final class CallUpdateRequestTest extends AnyWordSpec {

  "does not allow to set url when twiml is already set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |      import com.dixa.twilio.model.callback.CallbackUrl
        |      import com.dixa.twilio.model.iam.TwilioAccount
        |      import com.dixa.twilio.model.twiml.Response
        |      import com.dixa.twilio.model.voice.Call
        |      CallUpdateRequestExecutor.CallUpdateRequest.build(
        |        _.withAccountSid(TwilioAccount.Sid.unsafe("wow"))
        |          .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |          .withCallSid(Call.Sid.unsafe("hey"))
        |          .withUrl(CallbackUrl("weu"))
        |          .build()
        |      )""".stripMargin
    )
  }

  "requires accountSid to be set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |    import com.dixa.twilio.model.twiml.Response
        |    import com.dixa.twilio.model.voice.Call
        |    CallUpdateRequestExecutor.CallUpdateRequest.build(
        |      _.withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |        .withCallSid(Call.Sid.unsafe("hey"))
        |        .build()
        |    )""".stripMargin
    )
  }

}
