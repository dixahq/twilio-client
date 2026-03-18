// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
