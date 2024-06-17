package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.ChannelSenderCreateRequestExecutor.ChannelSenderCreateException
import com.dixa.twilio.client.messaging.{ChannelSenderCreateRequestExecutor, TwilioClientMessaging}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ChannelSender.Webhooks
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ChannelSenderCreateTest extends TwilioClientTest {

  classOf[TwilioClientMessaging].getSimpleName when {
    "Asked to create a channel sender" should {

      "Call Twilio to create a Whatsapp sender" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioResponse))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelSenderSid.toString)
            )
        )

        val expected = Right(channelSenderSid)

        val resultFut: Future[
          Either[ChannelSenderCreateException, ChannelSender.Sid]
        ] =
          instance.run(connSettings, createRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "Call Twilio to create a Whatsapp sender with phonenumber sender id and get exception" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioResponse1))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelSenderSid.toString)
            )
        )

        val expected = Left(ChannelSenderCreateException.ChannelNotSupported("PhoneNumberE164"))

        val resultFut: Future[
          Either[ChannelSenderCreateException, ChannelSender.Sid]
        ] =
          instance.run(connSettings, createRequest1)
        resultFut.map { result => assert(result === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val channelSenderSid = ChannelSender.Sid.unsafe("XEcfd04c72e3397a53e24bd6c7408aff83")

    val createRequest = ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest(
      senderId = WhatsappNumber.unsafe("whatsapp:+4552511283"),
      configuration = ChannelSender.Configuration.WhatsappVerificationMethod(
        ChannelSender.VerificationMethod.SMS
      ),
      webhooks = Webhooks(None, None, None),
      profile = ChannelSender.Profile
        .WhatsappProfile(about = "", phoneNumberDisplayName = "Dixa Twilio WABA")
    )

    val createRequest1 = ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest(
      senderId = PhoneNumberE164.unsafe("+4552511283"),
      configuration = ChannelSender.Configuration.WhatsappVerificationMethod(
        ChannelSender.VerificationMethod.SMS
      ),
      webhooks = Webhooks(None, None, None),
      profile = ChannelSender.Profile
        .WhatsappProfile(about = "", phoneNumberDisplayName = "Dixa Twilio WABA")
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelSenderCreateRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelSenderCreate
  }

  private def createChannelWhatsappSenderTwilioResponse =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "about": "",
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "webhook": {
      |        "callback_url": "",
      |        "callback_method": "",
      |        "fallback_url": "",
      |        "fallback_method": "",
      |        "status_callback_url": "",
      |        "status_callback_method": ""
      |    },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelWhatsappSenderTwilioResponse1 =
    """{
      |    "sender_id": "+4552511283",
      |    "profile": {
      |        "about": "",
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "webhook": {
      |        "callback_url": "",
      |        "callback_method": "",
      |        "fallback_url": "",
      |        "fallback_method": "",
      |        "status_callback_url": "",
      |        "status_callback_method": ""
      |    },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin
}
