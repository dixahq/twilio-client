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
import com.dixa.twilio.client.content.ContentReadRequestExecutor
import com.dixa.twilio.client.content.ContentReadRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.{ContentTemplate, ContentType}
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.Instant

final class ContentReadTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentReadRequestExecutor].getSimpleName when {
    "asked to list content templates" should {
      "return all templates across multiple pages" in {
        val sid2 = "HXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"

        val template2 = ContentTemplate(
          sid = ContentTemplate.Sid.unsafe(sid2),
          accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          friendlyName = "api_rating_request",
          language = "en",
          variables = Map("1" -> "John"),
          types = Map("twilio/text" -> ContentType.Text("Hi {{1}}, \nPlease rate us!")),
          dateCreated = Instant.parse("2026-05-20T11:30:50Z"),
          dateUpdated = Instant.parse("2026-05-20T11:50:22Z")
        )

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v1/Content"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(page1Response(sid2))
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v1/Content"))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam("PageToken", WireMock.equalTo("DNHXXXXXXX"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(page2Response(sid2))
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ContentReadRequestExecutor = TwilioClient.defaultImpl().content.contentRead
        val req                                  = ContentReadRequest.build(_.build())

        val resultSource: Source[Either[ContentReadException, ContentTemplate], NotUsed] =
          instance.source(connSettings, req)
        resultSource.runWith(Sink.seq).map { results =>
          assert(results.toSet === Set(Right(textTemplate), Right(template2)))
        }
      }
    }
  }

  private def page1Response(sid2: String) =
    s"""|{
        |  "contents": [${contentTemplateJson()}],
        |  "meta": {
        |    "key": "contents",
        |    "page": 0,
        |    "page_size": 1,
        |    "first_page_url": "http://localhost:${wireMockServer.port()}/v1/Content?Page=0",
        |    "next_page_url": "http://localhost:${wireMockServer
         .port()}/v1/Content?Page=1&PageToken=DNHXXXXXXX",
        |    "previous_page_url": null,
        |    "url": "http://localhost:${wireMockServer.port()}/v1/Content?Page=0"
        |  }
        |}""".stripMargin

  private def page2Response(sid2: String) =
    s"""|{
        |  "contents": [
        |    {
        |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        |      "date_created": "2026-05-20T11:30:50Z",
        |      "date_updated": "2026-05-20T11:50:22Z",
        |      "friendly_name": "api_rating_request",
        |      "language": "en",
        |      "sid": "$sid2",
        |      "types": { "twilio/text": { "body": "Hi {{1}}, \\nPlease rate us!" } },
        |      "variables": { "1": "John" }
        |    }
        |  ],
        |  "meta": {
        |    "key": "contents",
        |    "page": 1,
        |    "page_size": 1,
        |    "first_page_url": "http://localhost:${wireMockServer.port()}/v1/Content?Page=0",
        |    "next_page_url": null,
        |    "previous_page_url": "http://localhost:${wireMockServer.port()}/v1/Content?Page=0",
        |    "url": "http://localhost:${wireMockServer
         .port()}/v1/Content?Page=1&PageToken=DNHXXXXXXX"
        |  }
        |}""".stripMargin
}
