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

import com.dixa.twilio.client.content.ContentApprovalCreateRequestExecutor
import com.dixa.twilio.client.content.ContentApprovalCreateRequestExecutor._
import com.dixa.twilio.client.content.ContentSharedFixture
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.content.ContentApproval
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson}

import scala.concurrent.Future

final class ContentApprovalCreateTest extends TwilioClientTest with ContentSharedFixture {

  classOf[ContentApprovalCreateRequestExecutor].getSimpleName when {
    "building a request" should {
      "reject names with uppercase letters" in {
        assertThrows[IllegalArgumentException] {
          ContentApprovalCreateRequest.build(
            _.withContentSid(contentSid)
              .withName("My_Template")
              .withCategory(ContentApproval.WhatsappCategory.Utility)
              .build()
          )
        }
      }
      "reject names with spaces" in {
        assertThrows[IllegalArgumentException] {
          ContentApprovalCreateRequest.build(
            _.withContentSid(contentSid)
              .withName("my template")
              .withCategory(ContentApproval.WhatsappCategory.Utility)
              .build()
          )
        }
      }
      "reject names with hyphens" in {
        assertThrows[IllegalArgumentException] {
          ContentApprovalCreateRequest.build(
            _.withContentSid(contentSid)
              .withName("my-template")
              .withCategory(ContentApproval.WhatsappCategory.Utility)
              .build()
          )
        }
      }
      "accept names with only lowercase letters, digits, and underscores" in {
        ContentApprovalCreateRequest.build(
          _.withContentSid(contentSid)
            .withName("my_template_123")
            .withCategory(ContentApproval.WhatsappCategory.Utility)
            .build()
        )
        succeed
      }
    }
    "asked to submit a WhatsApp approval request" should {
      "return the approval on 201" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(equalToJson(requestBody))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(approvalCreateResponse)
            )
        )

        val expected = Right(
          ContentApproval(
            sid = contentSid,
            accountSid = None,
            whatsapp = Some(
              ContentApproval.WhatsappApproval(
                name = Some("my_utility_template"),
                category = Some("UTILITY"),
                contentType = Some("twilio/text"),
                status = ContentApproval.ApprovalStatus.Received,
                rejectionReason = None,
                allowCategoryChange = false
              )
            )
          )
        )

        val resultFut: Future[Either[ContentApprovalCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result => assert(result === expected))
      }

      "return TemplateTooLong on 400 with code 21658" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """|{
                     |  "code": 21658,
                     |  "message": "Body cannot exceed 1024 characters",
                     |  "more_info": "https://www.twilio.com/docs/errors/21658",
                     |  "status": 400
                     |}""".stripMargin
                )
            )
        )

        val resultFut: Future[Either[ContentApprovalCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(result === Left(ContentApprovalCreateException.TemplateTooLong))
        )
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

        val resultFut: Future[Either[ContentApprovalCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(result === Left(ContentApprovalCreateException.ContentNotFound(contentSid)))
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

        val resultFut: Future[Either[ContentApprovalCreateException, _]] =
          instance.run(connSettings, req)
        resultFut.map(result =>
          assert(
            result === Left(
              ContentApprovalCreateException.Api(ApiException.AuthenticationException())
            )
          )
        )
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ContentApprovalCreateRequestExecutor =
      TwilioClient.defaultImpl().content.contentApprovalCreate

    val req = ContentApprovalCreateRequest.build(
      _.withContentSid(contentSid)
        .withName("my_utility_template")
        .withCategory(ContentApproval.WhatsappCategory.Utility)
        .build()
    )

    val requestBody =
      """|{
         |  "name": "my_utility_template",
         |  "category": "UTILITY"
         |}""".stripMargin

    val approvalCreateResponse =
      """|{
         |  "name": "my_utility_template",
         |  "category": "UTILITY",
         |  "content_type": "twilio/text",
         |  "status": "received",
         |  "rejection_reason": ""
         |}""".stripMargin

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(WireMock.urlPathEqualTo(s"/v1/Content/$contentSid/ApprovalRequests/whatsapp"))
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/json"))
  }
}
