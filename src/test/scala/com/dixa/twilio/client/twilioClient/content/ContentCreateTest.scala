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

package com.dixa.twilio.client.twilioClient.content

import com.dixa.twilio.client.content.ContentCreateRequestExecutor
import com.dixa.twilio.client.content.ContentCreateRequestExecutor._
import com.dixa.twilio.client.content.ContentSharedFixture
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.ContentType
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ContentCreateTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentCreateRequestExecutor].getSimpleName when {
    "asked to create a text template" should {
      "return the created template on 201" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(requestBody, true, true))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(contentTemplateJson())
            )
        )

        val resultFut: Future[Either[ContentCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result => assert(result === Right(textTemplate)))
      }

      "return the created template on 200" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(requestBody, true, true))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(contentTemplateJson())
            )
        )

        val resultFut: Future[Either[ContentCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result => assert(result === Right(textTemplate)))
      }

      "return Api exception on 401" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioAuthErrorJson)
            )
        )

        val resultFut: Future[Either[ContentCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(
            result === Left(ContentCreateException.Api(ApiException.AuthenticationException()))
          )
        )
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ContentCreateRequestExecutor = TwilioClient.defaultImpl().content.contentCreate

    val req = ContentCreateRequest.build(
      _.withFriendlyName("dixa_support_ticket_changed")
        .withLanguage("en")
        .withVariables(Map("1" -> "John Doe", "2" -> "123456"))
        .withTypes(
          Map(
            "twilio/text" -> ContentType.Text(
              "Hello, {{1}}.\n Thanks for contacting Dixa Support. Your ticket number is #{{2}}. We will be in touch shortly. Check for more contact options on our website."
            )
          )
        )
        .build()
    )

    val requestBody =
      """|{
         |  "friendly_name": "dixa_support_ticket_changed",
         |  "language": "en",
         |  "variables": { "1": "John Doe", "2": "123456" },
         |  "types": {
         |    "twilio/text": {
         |      "body": "Hello, {{1}}.\n Thanks for contacting Dixa Support. Your ticket number is #{{2}}. We will be in touch shortly. Check for more contact options on our website."
         |    }
         |  }
         |}""".stripMargin

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(WireMock.urlPathEqualTo("/v1/Content"))
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/json"))
  }
}
