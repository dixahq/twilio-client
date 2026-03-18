// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.TwilioMessagingService
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URL
import scala.concurrent.Future

final class MessagingServiceCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask complete create a Service" should {
      "ask twilio to send it, and return the Service it gets back from Twilio" in {

        val toCreate = TwilioClientMessaging.ServiceCreateRequest(
          friendlyName = TwilioMessagingService.FriendlyName("A new service"),
          inboundRequestWebhook = Some(
            TwilioMessagingService
              .InboundRequestWebhook(HttpMethod.Get, new URL("https://www.inbound.com/"))
          ),
          fallbackWebhook = None,
          statusCallback = Some(MessageStatusCallback(new URL("https://www.status.com"))),
          useInboundWebhookOnNumber = TwilioMessagingService.UseInboundWebhookOnNumber.True
        )

        val encodedInboundUrl = "https%3A%2F%2Fwww.inbound.com%2F"
        val encodedStatusCb   = "https%3A%2F%2Fwww.status.com"
        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                "/v1/Services"
              )
            )
            .withRequestBody(WireMock.containing("FriendlyName=A+new+service"))
            .withRequestBody(WireMock.containing(s"InboundRequestUrl=$encodedInboundUrl"))
            .withRequestBody(WireMock.containing("InboundMethod=GET"))
            .withRequestBody(WireMock.containing(s"StatusCallback=$encodedStatusCb"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = TwilioMessagingService(
          TwilioMessagingService.Sid.unsafe("MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXX678"),
          TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXX354"),
          toCreate.friendlyName,
          toCreate.inboundRequestWebhook,
          toCreate.fallbackWebhook,
          toCreate.statusCallback,
          toCreate.useInboundWebhookOnNumber
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance     = TwilioClient.defaultImpl().messaging
        val resultFut: Future[TwilioMessagingService] =
          instance.serviceCreate(connSettings, toCreate)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

  private def twilioResponse1 =
    """{
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXX354",
      |      "friendly_name": "A new service",
      |      "sid": "MGXXXXXXXXXXXXXXXXXXXXXXXXXXXXX678",
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
      |      "inbound_request_url": "https://www.inbound.com/",
      |      "inbound_method": "GET",
      |      "fallback_url": null,
      |      "fallback_method": "POST",
      |      "status_callback": "https://www.status.com",
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
      |      }
      |}
      |""".stripMargin
}
