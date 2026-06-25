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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.client.content.ContentSearchRequestExecutor
import com.dixa.twilio.client.content.ContentSearchRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.ContentTemplate
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class ContentSearchTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentSearchRequestExecutor].getSimpleName when {
    "asked to search content templates" should {
      "return matching templates filtered by language and channel eligibility" in {
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v2/Content"))
            .withQueryParam("Language", WireMock.equalTo("en"))
            .withQueryParam("ChannelEligibility", WireMock.equalTo("whatsapp:unsubmitted"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(singlePageResponse)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ContentSearchRequestExecutor =
          TwilioClient.defaultImpl().content.contentSearch
        val req = ContentSearchRequest.build(
          _.withLanguage("en")
            .withChannelEligibility(ChannelEligibility("whatsapp", "unsubmitted"))
            .build()
        )

        val resultSource: Source[Either[ContentSearchException, ContentTemplate], NotUsed] =
          instance.source(connSettings, req)
        resultSource.runWith(Sink.seq).map { results =>
          assert(results === List(Right(textTemplate)))
        }
      }

      "return all templates when no filters are applied" in {
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v2/Content"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(singlePageResponse)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ContentSearchRequestExecutor =
          TwilioClient.defaultImpl().content.contentSearch
        val req = ContentSearchRequest.build(_.build())

        val resultSource: Source[Either[ContentSearchException, ContentTemplate], NotUsed] =
          instance.source(connSettings, req)
        resultSource.runWith(Sink.seq).map { results =>
          assert(results === List(Right(textTemplate)))
        }
      }
    }
  }

  private def singlePageResponse =
    s"""|{
        |  "contents": [${contentTemplateJson()}],
        |  "meta": {
        |    "key": "contents",
        |    "page": 0,
        |    "page_size": 50,
        |    "first_page_url": "http://localhost:${wireMockServer.port()}/v2/Content?Page=0",
        |    "next_page_url": null,
        |    "previous_page_url": null,
        |    "url": "http://localhost:${wireMockServer.port()}/v2/Content?Page=0"
        |  }
        |}""".stripMargin
}
