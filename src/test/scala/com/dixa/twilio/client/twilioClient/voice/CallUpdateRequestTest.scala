package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
import org.scalatest.wordspec.AnyWordSpec

// The test results are cached so may need to run sbt clean when test code is changed

final class CallUpdateRequestTest extends AnyWordSpec {

  classOf[CallUpdateRequestExecutor.CallUpdateRequest].getSimpleName when {

    "accountSid is not set" should {
      "require accountSid" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.voice.Call
            |
            |    CallUpdateRequestExecutor.CallUpdateRequest.build(
            |      _.withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withCallSid(Call.Sid.unsafe("some_call_sid"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "callSid is not set" should {
      "require callSid" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |  import com.dixa.twilio.model.callback.CallbackUrl
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |
            |  CallUpdateRequestExecutor.CallUpdateRequest.build(
            |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |      .withUrl(CallbackUrl("some_url"))
            |      .build()
            |  )""".stripMargin
        )
      }
    }

    "request is created with required accountSid and callSid attributes" should {
      "require at least one update attribute" in {
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

    "both twiml and url are set" should {
      "not allow to have both because only one is allowed" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |      import com.dixa.twilio.model.callback.CallbackUrl
            |      import com.dixa.twilio.model.iam.TwilioAccount
            |      import com.dixa.twilio.model.twiml.Response
            |      import com.dixa.twilio.model.voice.Call
            |
            |      CallUpdateRequestExecutor.CallUpdateRequest.build(
            |        _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |          .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |          .withCallSid(Call.Sid.unsafe("some_call_sid"))
            |          .withUrl(CallbackUrl("some_url"))
            |          .build()
            |      )""".stripMargin
        )
      }
    }

    "method is set" should {
      "require url to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |  import com.dixa.twilio.model.twiml.Response
            |  import com.dixa.twilio.model.voice.Call
            |  import com.dixa.twilio.model.HttpMethod
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
    }

    "fallbackMethod is set" should {
      "require fallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |  import com.dixa.twilio.model.twiml.Response
            |  import com.dixa.twilio.model.voice.Call
            |  import com.dixa.twilio.model.HttpMethod
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
    }

    "statusCallbackMethod is set" should {
      "require statusCallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallUpdateRequestExecutor
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |  import com.dixa.twilio.model.twiml.Response
            |  import com.dixa.twilio.model.voice.Call
            |  import com.dixa.twilio.model.HttpMethod
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
    }
  }
}
