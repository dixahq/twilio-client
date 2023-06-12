package com.dixa.twilio.client.twilioClient.voice

import org.scalatest.wordspec.AnyWordSpec

// The test results are cached so may need to run sbt clean when test code is changed

final class CallCreateRequestTest extends AnyWordSpec {

  "requires accountSid" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
        |  import com.dixa.twilio.model.voice.Call
        |  CallCreateRequestExecutor.CallCreateRequest.build(
        |    _.withToCallerId(Call.CallerId("some callee"))
        |      .withFromCallerId(Call.CallerId("some caller"))
        |      .build()
        |  )""".stripMargin
    )
  }

  "requires toCallerId" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.voice.Call
        |  CallCreateRequestExecutor.CallCreateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("wow"))
        |      .withFromCallerId(Call.CallerId("some caller"))
        |      .build()
        |  )""".stripMargin
    )
  }

  "requires fromCallerId" in {
    assertTypeError(
      """import com.dixa.twilio.client.voice.CallCreateRequestExecutor
        |  import com.dixa.twilio.model.iam.TwilioAccount
        |  import com.dixa.twilio.model.voice.Call
        |  CallCreateRequestExecutor.CallCreateRequest.build(
        |    _.withAccountSid(TwilioAccount.Sid.unsafe("wow"))
        |      .withToCallerId(Call.CallerId("some callee"))
        |      .build()
        |  )""".stripMargin
    )
  }

}
