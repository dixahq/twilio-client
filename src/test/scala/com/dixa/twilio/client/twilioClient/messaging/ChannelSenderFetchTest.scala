package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.ChannelSenderFetchRequestExecutor.ChannelSenderFetchException
import com.dixa.twilio.client.messaging.{ChannelSenderFetchRequestExecutor, TwilioClientMessaging}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ChannelSender.Webhooks
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ChannelSenderFetchTest extends TwilioClientTest {

  classOf[TwilioClientMessaging].getSimpleName when {
    "Asked to fetch an channel sender" should {

      "Return the whatsapp channel sender that it receives from twilio" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelWhatsappSenderTwilioResponse1)
            )
        )

        val expected = Right(whatsappChannelSender)

        val resultFut: Future[
          Either[ChannelSenderFetchException, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map { result => assert(result === expected) }
      }

      "Return exception if channel sender id isn't supported" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelUnknownSenderTwilioResponse1)
            )
        )

        val expected = ChannelSenderFetchException.ParseFailure(
          "Channel Sender id @twitterhandel of unknown type not supported"
        )

        val resultFut: Future[
          Either[ChannelSenderFetchException, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map {
          case Left(ex) => assert(ex == expected)
          case Right(_) => fail("should return left exception")
        }
      }

      "Return exception if channel sender id is e164 formatted phone number" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(channelTelephonySenderTwilioResponse1)
            )
        )

        val expected = ChannelSenderFetchException.ParseFailure(
          "PhoneNumber Channel Sender with id +4552511283 not supported"
        )

        val resultFut: Future[
          Either[ChannelSenderFetchException, ChannelSender]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map {
          case Left(ex) => assert(ex == expected)
          case Right(_) => fail("should return left exception")
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val channelSenderSid = ChannelSender.Sid.unsafe("XEcfd04c72e3397a53e24bd6c7408aff83")
    val whatsappChannelSender: ChannelSender = ChannelSender.WhatsappSender(
      status = ChannelSender.Status.Online,
      profile = ChannelSender.Profile("Dixa Twilio WABA"),
      senderId = WhatsappNumber.unsafe("whatsapp:+4552511283"),
      sid = channelSenderSid,
      webhooks = Webhooks(None, None, None),
      configuration = ChannelSender.Configuration.WabaId("316806161514452")
    )

    val fetchRequest = ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest(
      channelSenderSid = channelSenderSid,
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelSenderFetchRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelSenderFetch
  }

  private def channelWhatsappSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "whatsapp:+4552511283",
      |    "webhook": {
      |        "fallback_method": "",
      |        "fallback_url": "",
      |        "status_callback_url": "",
      |        "callback_method": "",
      |        "callback_url": "",
      |        "status_callback_method": ""
      |    },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    }
      |}
      |""".stripMargin

  private def channelUnknownSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "@twitterhandel",
      |    "webhook": {
      |        "fallback_method": "",
      |        "fallback_url": "",
      |        "status_callback_url": "",
      |        "callback_method": "",
      |        "callback_url": "",
      |        "status_callback_method": ""
      |    },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    }
      |}
      |""".stripMargin

  private def channelTelephonySenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Dixa Twilio WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "+4552511283",
      |    "webhook": {
      |        "fallback_method": "",
      |        "fallback_url": "",
      |        "status_callback_url": "",
      |        "callback_method": "",
      |        "callback_url": "",
      |        "status_callback_method": ""
      |    },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    }
      |}
      |""".stripMargin
}
