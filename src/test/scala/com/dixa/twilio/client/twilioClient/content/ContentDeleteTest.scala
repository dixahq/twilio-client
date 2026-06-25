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

import com.dixa.twilio.client.content.ContentDeleteRequestExecutor
import com.dixa.twilio.client.content.ContentDeleteRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ContentDeleteTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentDeleteRequestExecutor].getSimpleName when {
    "asked to delete a content template" should {
      "succeed on 204" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(aResponse().withStatus(204))
        )

        val resultFut: Future[Either[ContentDeleteException, _]] =
          instance.run(connSettings, req)
        resultFut.map {
          case Right(_) => succeed
          case Left(e)  => fail(e)
        }
      }

      "return ContentNotFound on 404" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioNotFoundJson)
            )
        )

        val resultFut: Future[Either[ContentDeleteException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(result === Left(ContentDeleteException.ContentNotFound(contentSid)))
        )
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

        val resultFut: Future[Either[ContentDeleteException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(
            result === Left(ContentDeleteException.Api(ApiException.AuthenticationException()))
          )
        )
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ContentDeleteRequestExecutor = TwilioClient.defaultImpl().content.contentDelete

    val req = ContentDeleteRequest.build(_.withContentSid(contentSid).build())

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .delete(WireMock.urlPathEqualTo(s"/v1/Content/$contentSid"))
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
  }
}
