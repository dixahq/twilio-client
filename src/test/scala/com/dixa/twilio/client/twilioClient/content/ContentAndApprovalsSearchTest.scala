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
import com.dixa.twilio.client.content.ContentAndApprovalsSearchRequestExecutor
import com.dixa.twilio.client.content.ContentAndApprovalsSearchRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.ContentTemplateWithApproval
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class ContentAndApprovalsSearchTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentAndApprovalsSearchRequestExecutor].getSimpleName when {
    "asked to search content templates with approval data" should {
      "return matching templates with approvals filtered by language and channel eligibility" in {
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo("/v2/ContentAndApprovals"))
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
        val instance: ContentAndApprovalsSearchRequestExecutor =
          TwilioClient.defaultImpl().content.contentAndApprovalsSearch
        val req = ContentAndApprovalsSearchRequest.build(
          _.withLanguage("en")
            .withChannelEligibility(ChannelEligibility("whatsapp", "unsubmitted"))
            .build()
        )

        val resultSource: Source[
          Either[ContentAndApprovalsSearchException, ContentTemplateWithApproval],
          NotUsed
        ] =
          instance.source(connSettings, req)
        resultSource.runWith(Sink.seq).map { results =>
          assert(results === List(Right(textTemplateWithUnsubmittedApproval)))
        }
      }
    }
  }

  private def singlePageResponse =
    s"""|{
        |  "contents": [${contentTemplateWithApprovalJson()}],
        |  "meta": {
        |    "key": "contents",
        |    "page": 0,
        |    "page_size": 50,
        |    "first_page_url": "http://localhost:${wireMockServer
         .port()}/v2/ContentAndApprovals?Page=0",
        |    "next_page_url": null,
        |    "previous_page_url": null,
        |    "url": "http://localhost:${wireMockServer.port()}/v2/ContentAndApprovals?Page=0"
        |  }
        |}""".stripMargin
}
