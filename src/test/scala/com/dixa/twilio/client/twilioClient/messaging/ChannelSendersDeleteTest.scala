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

import com.dixa.twilio.client.ApiException.{BadRequestException, NotFound}
import com.dixa.twilio.client.{TwilioClient, TwilioConnectionSettings, TwilioTestConstants}
import com.dixa.twilio.client.messaging.{
  ChannelSendersException,
  ChannelsSendersDeleteRequestExecutor
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.model.FUnit
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class ChannelSendersDeleteTest extends TwilioClientTest with ChannelSendersTestSharedFixture {

  "TwilioClientMessaging" when {
    "asked to delete a channel sender" should {

      "succeed" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === Right(FUnit)) }
      }

      "fail with bad request exception on bad request status code" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("you asked wrong")
            )
        )

        val expected =
          Left(ChannelSendersException.Api(BadRequestException("you asked wrong")))

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === expected) }
      }

      "fail with not found exception on not found status code" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteSenderNotFoundError)
            )
        )

        val expected =
          Left(ChannelSendersException.Api(NotFound(deleteSenderNotFoundError)))

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === expected) }
      }
    }
  }

  final class Fixture {
    val deleteReq: ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest =
      ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest(channelSenderSid =
        channelSenderSid
      )

    val wireMockBuilderExpectedTwilioRequest: MappingBuilder = WireMock
      .delete(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings: TwilioConnectionSettings =
      TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelsSendersDeleteRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelsSendersDelete

    val deleteSenderNotFoundError: String = {
      """{
        |"code": 20404,
        |"message": "The requested resource /Channels/Senders/XE58fa6d2ac8c6a868bcc6eab630fd7777 was not found",
        |"more_info": "https://www.twilio.com/docs/errors/20404",
        |"status": 404
        |}""".stripMargin
    }
  }
}
