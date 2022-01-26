package com.dixa.twilio.client.twilioClient.messaging

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.client.model.HttpMethod
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioClientMessaging, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URL
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final class ReadServicesTest extends TwilioClientTest {

  classOf[TwilioClientMessaging].getSimpleName when {

    "asked to read all service" should {

      "return all the services it gets from twilio" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v1/Services"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("ReadServicesTwilioResponse1.json")
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v1/Services"))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam(
              "PageToken",
              WireMock.equalTo(
                "PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("ReadServicesTwilioResponse2.json")
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientMessaging = TwilioClient.defaultImpl().messaging

        val resultSource: Source[TwilioMessagingService, NotUsed] =
          instance.readServices(twilioConnectionSetting)
        val resultFut =
          resultSource.runWith(Sink.seq)

        val expected = Seq(
          TwilioMessagingService(
            sid = TwilioMessagingService.Sid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My Service!"),
            inboundRequestWebhook = Some(
              TwilioMessagingService
                .InboundRequestWebhook(HttpMethod.Post, new URL("https://www.example.com/"))
            ),
            fallbackWebhook = None,
            statusCallback =
              Some(TwilioMessagingService.StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.False
          ),
          TwilioMessagingService(
            sid = TwilioMessagingService.Sid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My Secondary Service!"),
            inboundRequestWebhook = Some(
              TwilioMessagingService
                .InboundRequestWebhook(HttpMethod.Get, new URL("https://www.example.com/"))
            ),
            fallbackWebhook = None,
            statusCallback =
              Some(TwilioMessagingService.StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.True
          ),
          TwilioMessagingService(
            sid = TwilioMessagingService.Sid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My third Service!"),
            inboundRequestWebhook = None,
            fallbackWebhook = Some(
              TwilioMessagingService
                .FallbackWebhook(HttpMethod.Post, new URL("https://fallback.dixa.com"))
            ),
            statusCallback =
              Some(TwilioMessagingService.StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.False
          )
        )
        val result = Await.result(resultFut, 15.seconds)
        assert(result === expected)
      }
    }
  }
}
