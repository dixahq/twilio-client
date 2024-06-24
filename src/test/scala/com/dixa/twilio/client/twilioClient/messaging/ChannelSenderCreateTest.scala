package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.{
  ChannelSenderCreateRequestExecutor,
  ChannelSenderException,
  TwilioClientMessaging
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod.Post
import com.dixa.twilio.model.messaging.ChannelSender.Webhooks
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ChannelSenderCreateTest extends TwilioClientTest with ChannelSenderTestSharedFixture {

  classOf[TwilioClientMessaging].getSimpleName when {
    "Asked to create a channel sender" should {

      "Call Twilio to create a Whatsapp sender" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelWhatsappSenderTwilioResponse)
            )
        )

        val expected = Right(
          whatsappChannelSender.copy(
            properties = None,
            webhooks = Webhooks(
              callback = Some(ChannelSender.Webhook(Post, "https://webhook.messages")),
              fallback = None,
              statusCallback = None
            )
          )
        )

        val resultFut: Future[
          Either[ChannelSenderException, ChannelSender]
        ] =
          instance.run(connSettings, createRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "Call Twilio to create a Whatsapp sender with phonenumber sender id and get exception" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest1))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelWhatsappSenderTwilioResponse)
            )
        )

        val expected = Left(ChannelSenderException.ChannelNotSupported("PhoneNumberE164"))

        val resultFut: Future[
          Either[ChannelSenderException, ChannelSender]
        ] =
          instance.run(connSettings, createRequest1)
        resultFut.map { result => assert(result === expected) }
      }

      "Call Twilio to create a Whatsapp sender with a waba id" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(createChannelWhatsappSenderTwilioRequest2))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(createChannelWhatsappSenderTwilioResponse)
            )
        )

        val req = ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest(
          senderId = WhatsappNumber.unsafe("whatsapp:+4552511283"),
          configuration = ChannelSender.Configuration(
            wabaId = Some("316806161514452"),
            verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
          ),
          webhooks = Webhooks(None, None, None),
          profile = ChannelSender.Profile
            .WhatsappProfile(phoneNumberDisplayName = "Dixa Twilio WABA")
        )

        val expected = Right(
          whatsappChannelSender.copy(
            properties = None,
            webhooks = Webhooks(
              callback = Some(ChannelSender.Webhook(Post, "https://webhook.messages")),
              fallback = None,
              statusCallback = None
            )
          )
        )

        val resultFut: Future[
          Either[ChannelSenderException, ChannelSender]
        ] =
          instance.run(connSettings, req)
        resultFut.map { result => assert(result === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val createRequest = ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest(
      senderId = WhatsappNumber.unsafe("whatsapp:+4552511283"),
      configuration = ChannelSender.Configuration(
        verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
      ),
      webhooks = Webhooks(None, None, None),
      profile = ChannelSender.Profile
        .WhatsappProfile(phoneNumberDisplayName = "Dixa Twilio WABA")
    )

    val createRequest1 = ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest(
      senderId = PhoneNumberE164.unsafe("+4552511283"),
      configuration = ChannelSender.Configuration(
        verificationMethod = Some(ChannelSender.VerificationMethod.SMS)
      ),
      webhooks = Webhooks(None, None, None),
      profile = ChannelSender.Profile
        .WhatsappProfile(phoneNumberDisplayName = "Dixa Twilio WABA")
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

  private def createChannelWhatsappSenderTwilioRequest =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelWhatsappSenderTwilioResponse =
    s"""{
       |    "status": "ONLINE",
       |    "profile": {
       |        "name": "Dixa Twilio WABA"
       |    },
       |    "offline_reasons": null,
       |    "sender_id": "whatsapp:+4552511283",
       |    "webhook": {
       |        "callback_method": "POST",
       |        "callback_url": "https://webhook.messages"
       |    },
       |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83",
       |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
       |    "configuration": {
       |        "waba_id": "316806161514452"
       |    },
       |    "properties": null
       |}
       |""".stripMargin

  private def createChannelWhatsappSenderTwilioRequest1 =
    """{
      |    "sender_id": "+4552511283",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin

  private def createChannelWhatsappSenderTwilioRequest2 =
    """{
      |    "sender_id": "whatsapp:+4552511283",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "webhook": { },
      |    "configuration": {
      |        "waba_id": "316806161514452",
      |        "verification_method": "sms"
      |    }
      |}
      |
      |""".stripMargin
}
