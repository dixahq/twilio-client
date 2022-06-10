package com.dixa.twilio.client.twilioClient.messaging

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.client.messaging.{ServicesReadRequestExecutor, TwilioClientMessaging}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{ServiceSid, StatusCallback, TwilioMessagingService}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URL

final class MessagingServicesReadV2Test extends TwilioClientTest {

  classOf[TwilioClientMessaging].getSimpleName when {

    "asked to read all service" should {

      "safely return all the services it gets from twilio" in {

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v1/Services"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
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
                .withBody(twilioResponse2)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientMessaging = TwilioClient.defaultImpl().messaging
        val req                             = ServicesReadRequestExecutor.ServicesReadRequest()
        val resultSource: Source[Either[ServicesReadException, TwilioMessagingService], NotUsed] =
          instance.servicesReadV2.source(twilioConnectionSetting, req)
        val resultFut =
          resultSource.runWith(Sink.seq)

        val expected = List(
          TwilioMessagingService(
            sid = ServiceSid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My Service!"),
            inboundRequestWebhook = Some(
              TwilioMessagingService
                .InboundRequestWebhook(HttpMethod.Post, new URL("https://www.example.com/"))
            ),
            fallbackWebhook = None,
            statusCallback = Some(StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.False
          ),
          TwilioMessagingService(
            sid = ServiceSid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My Secondary Service!"),
            inboundRequestWebhook = Some(
              TwilioMessagingService
                .InboundRequestWebhook(HttpMethod.Get, new URL("https://www.example.com/"))
            ),
            fallbackWebhook = None,
            statusCallback = Some(StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.True
          ),
          TwilioMessagingService(
            sid = ServiceSid("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"),
            accountSid = TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            friendlyName = TwilioMessagingService.FriendlyName("My third Service!"),
            inboundRequestWebhook = None,
            fallbackWebhook = Some(
              TwilioMessagingService
                .FallbackWebhook(HttpMethod.Post, new URL("https://fallback.dixa.com"))
            ),
            statusCallback = Some(StatusCallback(new URL("https://www.example.com"))),
            useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.False
          )
        )
        resultFut.map(result => assert(result.toSet === expected.map(Right(_)).toSet))
      }
    }
  }

  //format: off
  private def twilioResponse1 =    
    s"""{
      |  "meta": {
      |    "page": 0,
      |    "page_size": 2,
      |    "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
      |    "previous_page_url": null,
      |    "next_page_url": "http://localhost:${wireMockServer.port()}/v1/Services?PageSize=2&Page=1&PageToken=PTMGf9a4a36b7b901e4a5d325ff1d92c6dcd",
      |    "key": "services",
      |    "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0"
      |  },
      |  "services": [
      |    {
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "My Service!",
      |      "sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |      "date_created": "2015-07-30T20:12:31Z",
      |      "date_updated": "2015-07-30T20:12:33Z",
      |      "sticky_sender": true,
      |      "mms_converter": true,
      |      "smart_encoding": false,
      |      "fallback_to_long_code": true,
      |      "area_code_geomatch": true,
      |      "validity_period": 600,
      |      "scan_message_content": "inherit",
      |      "synchronous_validation": true,
      |      "inbound_request_url": "https://www.example.com/",
      |      "inbound_method": "POST",
      |      "fallback_url": null,
      |      "fallback_method": "POST",
      |      "status_callback": "https://www.example.com",
      |      "usecase": "marketing",
      |      "us_app_to_person_registered": false,
      |      "use_inbound_webhook_on_number": false,
      |      "links": {
      |        "phone_numbers": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/PhoneNumbers",
      |        "short_codes": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/ShortCodes",
      |        "alpha_senders": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/AlphaSenders",
      |        "messages": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Messages",
      |        "broadcasts": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Broadcasts",
      |        "us_app_to_person": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Compliance/Usa2p",
      |        "us_app_to_person_usecases": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Compliance/Usa2p/Usecases"
      |      },
      |      "url": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"
      |    },
      |    {
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "My Secondary Service!",
      |      "sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
      |      "date_created": "2016-07-30T20:12:31Z",
      |      "date_updated": "2016-07-30T20:12:33Z",
      |      "sticky_sender": true,
      |      "mms_converter": true,
      |      "smart_encoding": false,
      |      "fallback_to_long_code": true,
      |      "area_code_geomatch": true,
      |      "validity_period": 600,
      |      "scan_message_content": "inherit",
      |      "synchronous_validation": true,
      |      "inbound_request_url": "https://www.example.com/",
      |      "inbound_method": "GET",
      |      "fallback_url": null,
      |      "fallback_method": "POST",
      |      "status_callback": "https://www.example.com",
      |      "usecase": "marketing",
      |      "us_app_to_person_registered": false,
      |      "use_inbound_webhook_on_number": true,
      |      "links": {
      |        "phone_numbers": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/PhoneNumbers",
      |        "short_codes": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/ShortCodes",
      |        "alpha_senders": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/AlphaSenders",
      |        "messages": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Messages",
      |        "broadcasts": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Broadcasts",
      |        "us_app_to_person": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Compliance/Usa2p",
      |        "us_app_to_person_usecases": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Compliance/Usa2p/Usecases"
      |      },
      |      "url": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"
      |    }
      |  ]
      |}
      |""".stripMargin
      
  private def twilioResponse2 =
    """{
      |  "meta": {
      |    "page": 1,
      |    "page_size": 2,
      |    "first_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0",
      |    "previous_page_url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=0&PageToken=PTMGd8410e59416697cb4455c87eba98a6d0",
      |    "next_page_url": null,
      |    "key": "services",
      |    "url": "https://messaging.twilio.com/v1/Services?PageSize=2&Page=1&PageToken=PTMGd8410e59416697cb4455c87eba98a6d"
      |  },
      |  "services": [
      |    {
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "My third Service!",
      |      "sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3",
      |      "date_created": "2017-07-30T20:12:31Z",
      |      "date_updated": "2017-07-30T20:12:33Z",
      |      "sticky_sender": true,
      |      "mms_converter": true,
      |      "smart_encoding": false,
      |      "fallback_to_long_code": true,
      |      "area_code_geomatch": true,
      |      "validity_period": 600,
      |      "scan_message_content": "inherit",
      |      "synchronous_validation": true,
      |      "inbound_request_url": null,
      |      "inbound_method": "POST",
      |      "fallback_url": "https://fallback.dixa.com",
      |      "fallback_method": "POST",
      |      "status_callback": "https://www.example.com",
      |      "usecase": "marketing",
      |      "us_app_to_person_registered": false,
      |      "use_inbound_webhook_on_number": false,
      |      "links": {
      |        "phone_numbers": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/PhoneNumbers",
      |        "short_codes": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/ShortCodes",
      |        "alpha_senders": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/AlphaSenders",
      |        "messages": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Messages",
      |        "broadcasts": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Broadcasts",
      |        "us_app_to_person": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Compliance/Usa2p",
      |        "us_app_to_person_usecases": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Compliance/Usa2p/Usecases"
      |      },
      |      "url": "https://messaging.twilio.com/v1/Services/MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"
      |    }
      |  ]
      |}
      |""".stripMargin
  //format: on
}
