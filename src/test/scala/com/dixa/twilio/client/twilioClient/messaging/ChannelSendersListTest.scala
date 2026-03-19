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

import com.dixa.twilio.client.messaging.{ChannelSenderException, ChannelsSendersListRequestExecutor}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ChannelSendersListTest extends TwilioClientTest with ChannelSenderTestSharedFixture {

  "TwilioClientMessaging" when {
    "Asked to list channel senders" should {

      "Return a list of senders from Twilio" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(listSendersResponse)
            )
        )

        val resultFut: Future[
          Either[
            ChannelSenderException,
            ChannelsSendersListRequestExecutor.ChannelSendersListResponse
          ]
        ] =
          instance.run(connSettings, listRequest)

        resultFut.map { result =>
          assert(result.isRight)
          assert(result.toOption.get.senders.size === 1)
          assert(result.toOption.get.senders.head.sid === channelSenderSid)
        }
      }

      "Return empty list when no senders match" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(emptyListSendersResponse)
            )
        )

        val resultFut: Future[
          Either[
            ChannelSenderException,
            ChannelsSendersListRequestExecutor.ChannelSendersListResponse
          ]
        ] =
          instance.run(connSettings, listRequest)

        resultFut.map { result =>
          assert(result.isRight)
          assert(result.toOption.get.senders.isEmpty)
        }
      }

    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val listRequest = ChannelsSendersListRequestExecutor.ChannelSendersListRequest()

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo("/v2/Channels/Senders")
      )
      .withQueryParam("Channel", WireMock.equalTo("whatsapp"))
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersListRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersList
  }

  private def listSendersResponse =
    """{
      |  "senders": [
      |    {
      |      "status": "ONLINE",
      |      "profile": {
      |        "name": "Dixa Twilio WABA"
      |      },
      |      "url": "https://messaging.twilio.com/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83",
      |      "sender_id": "whatsapp:+4552511283",
      |      "webhook": {},
      |      "sid": "XEcfd04c72e3397a53e24bd6c7408aff83",
      |      "configuration": {
      |        "waba_id": "316806161514452"
      |      },
      |      "properties": {}
      |    }
      |  ]
      |}
      |""".stripMargin

  private def emptyListSendersResponse =
    """{
      |  "senders": []
      |}
      |""".stripMargin
}
