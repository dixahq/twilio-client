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

import com.dixa.twilio.client.content.ContentApprovalFetchRequestExecutor
import com.dixa.twilio.client.content.ContentApprovalFetchRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.ContentApproval
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ContentApprovalFetchTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentApprovalFetchRequestExecutor].getSimpleName when {
    "asked to fetch approval status" should {
      "return the approval with whatsapp data on 200" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(approvalFetchResponse)
            )
        )

        val expected = Right(
          ContentApproval(
            sid = contentSid,
            accountSid = Some(accountSid),
            whatsapp = Some(
              ContentApproval.WhatsappApproval(
                name = "api_rating_request",
                category = "MARKETING",
                contentType = "twilio/text",
                status = ContentApproval.ApprovalStatus.Approved,
                rejectionReason = None,
                allowCategoryChange = true
              )
            )
          )
        )

        val resultFut: Future[Either[ContentApprovalFetchException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result => assert(result === expected))
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

        val resultFut: Future[Either[ContentApprovalFetchException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(result === Left(ContentApprovalFetchException.ContentNotFound(contentSid)))
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

        val resultFut: Future[Either[ContentApprovalFetchException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(
            result === Left(
              ContentApprovalFetchException.Api(ApiException.AuthenticationException())
            )
          )
        )
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ContentApprovalFetchRequestExecutor =
      TwilioClient.defaultImpl().content.contentApprovalFetch

    val req = ContentApprovalFetchRequest.build(_.withContentSid(contentSid).build())

    val approvalFetchResponse =
      s"""|{
          |  "sid": "$contentSid",
          |  "account_sid": "$accountSid",
          |  "whatsapp": {
          |    "name": "api_rating_request",
          |    "category": "MARKETING",
          |    "content_type": "twilio/text",
          |    "status": "approved",
          |    "rejection_reason": "",
          |    "allow_category_change": true
          |  }
          |}""".stripMargin

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(WireMock.urlPathEqualTo(s"/v1/Content/$contentSid/ApprovalRequests"))
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
  }
}
