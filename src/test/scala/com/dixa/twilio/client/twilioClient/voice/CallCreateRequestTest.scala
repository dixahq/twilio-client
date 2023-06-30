package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.voice.CallCreateRequestExecutor
import org.scalatest.wordspec.AnyWordSpec

// The test results are cached so may need to run sbt clean when test code is changed

final class CallCreateRequestTest extends AnyWordSpec {

  classOf[CallCreateRequestExecutor.CallCreateRequest].getSimpleName when {

    "accountSid is not set" should {
      "require accountSid" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withUrl(CallbackUrl("some_url"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "toCallerId is not set" should {
      "require toCallerId" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withUrl(CallbackUrl("some_url"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "fromCallerId is not set" should {
      "require fromCallerId" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |    import com.dixa.twilio.model.voice.Call
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withUrl(CallbackUrl("some_url"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "method is set" should {
      "require url to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.HttpMethod
            |    import com.dixa.twilio.model.twiml.Response
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withMethod(HttpMethod.Post)
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "fallbackMethod is set" should {
      "require fallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.HttpMethod
            |    import com.dixa.twilio.model.twiml.Response
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withFallbackMethod(HttpMethod.Post)
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "statusCallbackMethod is set" should {
      "require statusCallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.HttpMethod
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withStatusCallbackMethod(HttpMethod.Post)
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "recordingStatusCallbackMethod is set" should {
      "require recordingStatusCallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.HttpMethod
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withRecordingStatusCallbackMethod(HttpMethod.Post)
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "asyncAmdStatusCallbackMethod is set" should {
      "require asyncAmdStatusCallbackUrl to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.HttpMethod
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withAsyncAmdStatusCallbackMethod(HttpMethod.Post)
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "url, twiml and applicationSid are not set" should {
      "require one of them to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "url is set" should {
      "not allow to also set twiml" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |    import com.dixa.twilio.model.twiml.Response
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withUrl(CallbackUrl("some_url"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .build()
            |    )""".stripMargin
        )
      }

      "not allow to also set applicationSid" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |    import com.dixa.twilio.model.iam.Application
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withUrl(CallbackUrl("some_url"))
            |        .withApplicationSid(Application.Sid.unsafe("some_app_sid"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "twiml is set" should {
      "not allow to set url" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.callback.CallbackUrl
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withUrl(CallbackUrl("some_url"))
            |        .build()
            |    )""".stripMargin
        )
      }

      "not allow to set applicationSid" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |    import com.dixa.twilio.model.iam.TwilioAccount
            |    import com.dixa.twilio.model.voice.Call
            |    import com.dixa.twilio.model.twiml.Response
            |    import com.dixa.twilio.model.iam.Application
            |
            |    CallCreateRequestExecutor.CallCreateRequest.build(
            |      _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |        .withToCallerId(Call.CallerId("some_callee"))
            |        .withFromCallerId(Call.CallerId("some_caller"))
            |        .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |        .withApplicationSid(Application.Sid.unsafe("some_app_sid"))
            |        .build()
            |    )""".stripMargin
        )
      }
    }

    "applicationSid is set" should {
      "not allow to set url" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |  import com.dixa.twilio.model.voice.Call
            |  import com.dixa.twilio.model.callback.CallbackUrl
            |  import com.dixa.twilio.model.iam.Application
            |
            |  CallCreateRequestExecutor.CallCreateRequest.build(
            |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |      .withToCallerId(Call.CallerId("some_callee"))
            |      .withFromCallerId(Call.CallerId("some_caller"))
            |      .withApplicationSid(Application.Sid.unsafe("some_app_sid"))
            |      .withUrl(CallbackUrl("some_url"))
            |      .build()
            |  )""".stripMargin
        )
      }

      "not allow to set twiml" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |  import com.dixa.twilio.model.iam.TwilioAccount
            |  import com.dixa.twilio.model.voice.Call
            |  import com.dixa.twilio.model.iam.Application
            |  import com.dixa.twilio.model.twiml.Response
            |
            |  CallCreateRequestExecutor.CallCreateRequest.build(
            |    _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |      .withToCallerId(Call.CallerId("some_callee"))
            |      .withFromCallerId(Call.CallerId("some_caller"))
            |      .withApplicationSid(Application.Sid.unsafe("some_app_sid"))
            |      .withTwiml(Response.build(_.addHangup(_.build()).buildVerified()))
            |      .build()
            |  )""".stripMargin
        )
      }
    }

    "applicationSid and ignored attributes are set" should {

      // Some cases are not tested because those generate more than one type error
      // due to other constraints:
      // "warn that method is ignored when applicationSid is set first"
      // "warn that method is ignored when method is set first"
      // "warn that url is ignored when applicationSid is set first"
      // "warn that url is ignored when url is set first"
      // "warn that fallbackMethod is ignored when applicationSid is set first"
      // "warn that statusCallbackMethod is ignored when applicationSid is set first"

      "warn that fallbackUrl is ignored when applicationSid is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withFallbackUrl(CallbackUrl("some_url"))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that fallbackUrl is ignored when fallbackUrl is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .withFallbackUrl(CallbackUrl("some_url"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that fallbackMethod is ignored when fallbackMethod is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.HttpMethod
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withFallbackUrl(CallbackUrl("some_url"))
            |            .withFallbackMethod(HttpMethod.Get)
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that statusCallback is ignored when applicationSid is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .withStatusCallback(CallbackUrl("some_url"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that statusCallback is ignored when statusCallback is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withStatusCallback(CallbackUrl("some_url"))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that statusCallbackMethod is ignored when statusCallbackMethod is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.HttpMethod
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withStatusCallback(CallbackUrl("some_url"))
            |            .withStatusCallbackMethod(HttpMethod.Post)
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that statusCallbackEvent is ignored when applicationSid is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .withStatusCallbackEvents(Seq(Call.ProgressEvent.Answered))
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that statusCallbackEvent is ignored when statusCallbackEvent is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.iam.TwimlApplication
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withStatusCallbackEvents(Seq(Call.ProgressEvent.Answered))
            |            .withApplicationSid(TwimlApplication.Sid.unsafe("some_sid"))
            |            .build()
            |        )""".stripMargin
        )
      }
    }

    "recording attributes are set without record" should {
      "require record for recordingChannel" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withRecordingChannels(Call.RecordingChannels.Dual)
            |            .build()
            |        )""".stripMargin
        )
      }

      "require record for recordingStatusCallback" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withRecordingStatusCallback(CallbackUrl("some_url"))
            |            .build()
            |        )""".stripMargin
        )
      }

      "require record for recordingStatusCallbackEvents" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withRecordingStatusCallbackEvents(Seq(Call.RecordingEvent.InProgress))
            |            .build()
            |        )""".stripMargin
        )
      }

      "require record for recordingTrack" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withRecordingTrack(Call.RecordingTrack.Both)
            |            .build()
            |        )""".stripMargin
        )
      }
    }

    "asyncAmdCallback is set" should {
      "require asyncAmd to be set" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withAsyncAmdStatusCallback(CallbackUrl("url"))
            |            .build()
            |        )""".stripMargin
        )
      }
    }

    "sendDigits and machineDetection are set" should {
      "warn that machineDetection is ignored when sendDigits is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.dtmf.DtmfString
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withSendDigits(DtmfString.fromStringOnlyDtmfDigitsUnsafe("bla"))
            |            .withMachineDetection(Call.MachineDetection.Enable)
            |            .build()
            |        )""".stripMargin
        )
      }

      "warn that machineDetection is ignored when machineDetection is set first" in {
        assertTypeError(
          """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
            |        import com.dixa.twilio.model.voice.Call
            |        import com.dixa.twilio.model.callback.CallbackUrl
            |        import com.dixa.twilio.model.iam.TwilioAccount
            |        import com.dixa.twilio.model.dtmf.DtmfString
            |
            |        CallCreateRequestExecutor.CallCreateRequest.build(
            |          _.withAccountSid(TwilioAccount.Sid.unsafe("some_account_sid"))
            |            .withToCallerId(Call.CallerId("some_callee"))
            |            .withFromCallerId(Call.CallerId("some_caller"))
            |            .withUrl(CallbackUrl("some_url"))
            |            .withMachineDetection(Call.MachineDetection.Enable)
            |            .withSendDigits(DtmfString.fromStringOnlyDtmfDigitsUnsafe("bla"))
            |            .build()
            |        )""".stripMargin
        )
      }
    }
  }
}
