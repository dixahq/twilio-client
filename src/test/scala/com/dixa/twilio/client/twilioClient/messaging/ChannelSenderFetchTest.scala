package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.{
  ChannelSenderException,
  ChannelsSendersFetchRequestExecutor
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ChannelSender
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ChannelSenderFetchTest extends TwilioClientTest with ChannelSenderTestSharedFixture {

  "TwilioClientMessaging" when {
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
          Either[ChannelSenderException, ChannelSender]
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

        val expected = ChannelSenderException.ParseFailure(
          "Channel Sender id @twitterhandel of unknown type not supported"
        )

        val resultFut: Future[
          Either[ChannelSenderException, ChannelSender]
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

        val expected = ChannelSenderException.ParseFailure(
          "PhoneNumber Channel Sender with id +4552511283 not supported"
        )

        val resultFut: Future[
          Either[ChannelSenderException, ChannelSender]
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
    val fetchRequest =
      ChannelsSendersFetchRequestExecutor.ChannelSenderFetchRequest(channelSenderSid =
        channelSenderSid
      )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersFetchRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersFetch
  }

  private def channelWhatsappSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "whatsapp:+4552511283",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin

  private def channelUnknownSenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "@twitterhandel",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin

  private def channelTelephonySenderTwilioResponse1 =
    """{
      |    "status": "ONLINE",
      |    "profile": {
      |        "name": "Example WABA"
      |    },
      |    "url": "https://messaging.twilio.com/v2/Channels/Senders/XEfb45b27913a995543c9ccf5be843ee4",
      |    "sender_id": "+4552511283",
      |    "webhook": { },
      |    "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |    "configuration": {
      |        "waba_id": "316806161514452"
      |    },
      |    "properties":{ }
      |}
      |""".stripMargin
}
