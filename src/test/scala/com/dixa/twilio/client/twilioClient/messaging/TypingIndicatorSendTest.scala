package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.TypingIndicatorSendRequestExecutor
import com.dixa.twilio.client.messaging.TypingIndicatorSendRequestExecutor.TypingIndicatorSendRequest
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.messaging.Message
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class TypingIndicatorSendTest extends TwilioClientTest {

  "TwilioClientMessaging" when {
    "sending a typing indicator" should {
      "succeed" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(201)
            )
        )

        val resultFut = instance.run(connSettings, request)
        resultFut.map(result => assert(result === Right(FUnit)))
      }

      "succeed when Twilio responds with a server error" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")
            )
        )

        val resultFut = instance.run(connSettings, request)
        resultFut.map(result => assert(result === Right(FUnit)))
      }
    }
  }

  final class Fixture {
    val messageSid = Message.Sid.unsafe("SMXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: TypingIndicatorSendRequestExecutor =
      TwilioClient.defaultImpl().messaging.typingIndicatorSend

    val request = TypingIndicatorSendRequest(messageSid = messageSid)

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(WireMock.urlPathEqualTo("/v2/Indicators/Typing.json"))
      .withRequestBody(WireMock.containing(s"messageId=${messageSid}&channel=whatsapp"))
      .withBasicAuth(
        TwilioTestConstants.accountSid.toString,
        TwilioTestConstants.authToken.asString
      )
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
  }
}
