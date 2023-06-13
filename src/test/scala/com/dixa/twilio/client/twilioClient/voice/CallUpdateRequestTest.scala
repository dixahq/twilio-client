package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.model.HttpMethod
import org.scalatest.wordspec.AnyWordSpec

// The test results are cached so may need to run sbt clean when test code is changed

final class CallUpdateRequestTest extends AnyWordSpec {

  "requires accountSid" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |    import com.dixa.twilio.model.twiml.Response
        |    import com.dixa.twilio.model.voice.Call
        |    CallUpdateRequestExecutor.CallUpdateRequest.build(
        |      _.withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |        .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |        .build()
        |    )""".stripMargin
    )
  }

  "requires callSid" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |  import com.dixa.twilio.model.callback.CallbackUrl
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  CallUpdateRequestExecutor.CallUpdateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |      .withUrl(CallbackUrl("some_url"))
        |      .build()
        |  )""".stripMargin
    )
  }

  "does not allow to set url when twiml is already set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |      import com.dixa.twilio.model.callback.CallbackUrl
        |      import com.dixa.twilio.model.iam.TwilioAccount
        |      import com.dixa.twilio.model.twiml.Response
        |      import com.dixa.twilio.model.voice.Call
        |      CallUpdateRequestExecutor.CallUpdateRequest.build(
        |        _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |          .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |          .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |          .withUrl(CallbackUrl("some_url"))
        |          .build()
        |      )""".stripMargin
    )
  }

  "requires url when method method is set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.twiml.Response
        |  import com.dixa.twilio.model.voice.Call
        |
        |  CallUpdateRequestExecutor.CallUpdateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |      .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |      .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |      .withMethod(HttpMethod.Post)
        |      .build()
        |  )""".stripMargin
    )
  }

  "requires fallbackUrl when fallbackMethod is set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.twiml.Response
        |  import com.dixa.twilio.model.voice.Call
        |
        |  CallUpdateRequestExecutor.CallUpdateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |      .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |      .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |      .withFallbackMethod(HttpMethod.Post)
        |      .build()
        |  )""".stripMargin
    )
  }

  "requires statusCallbackUrl when statusCallbackMethod is set" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.twiml.Response
        |  import com.dixa.twilio.model.voice.Call
        |
        |  CallUpdateRequestExecutor.CallUpdateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |      .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |      .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
        |      .withStatusCallBackMethod(HttpMethod.Post)
        |      .build()
        |  )""".stripMargin
    )
  }

  "requires at least one update" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.twiml.Response
        |  import com.dixa.twilio.model.voice.Call
        |
        |  CallUpdateRequestExecutor.CallUpdateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
        |      .withCallSid(Call.Sid.unsafe("some_call_sid"))
        |      .build()
        |  )""".stripMargin
    )
  }

}
