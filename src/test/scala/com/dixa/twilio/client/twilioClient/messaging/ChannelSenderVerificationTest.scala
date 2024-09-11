package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationException
import com.dixa.twilio.client.messaging.ChannelsSendersVerificationRequestExecutor
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ChannelSender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ChannelSenderVerificationTest extends TwilioClientTest {

  "TwilioClientMessaging" when {
    "Sending verification code" should {

      "Call Twilio to verify with code" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(verifyChannelSenderRequest))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val expected = Right(())

        val resultFut: Future[
          Either[ChannelSenderVerificationException, Unit]
        ] =
          instance.run(connSettings, createRequest)
        resultFut.map { result => assert(result === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val channelSenderSid = ChannelSender.Sid.unsafe("XEcfd04c72e3397a53e24bd6c7408aff83")

    val createRequest = ChannelsSendersVerificationRequestExecutor.ChannelSenderVerificationRequest(
      senderSid = channelSenderSid,
      verificationCode = ChannelSender.VerificationCodeConfiguration(
        "123456"
      ),
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersVerificationRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersVerification
  }

  private val verifyChannelSenderRequest =
    """{
      |    "configuration": {
      |        "verification_code": "123456"
      |    }
      |}
      |
      |""".stripMargin
}
