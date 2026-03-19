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

package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{CallRecordingCreateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.voice.{Call, Recording}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time._
import scala.concurrent.Future

final class CallRecordingCreateTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to create a call recording" should {

      "Support creating a new call recording" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected =
          Recording(
            accountSid = connSettings.accountSid,
            callSid = callSid,
            channels = Recording.Channels.unsafe(2),
            dateCreated = createdAtInstant,
            dateUpdate = updatedAtInstant,
            startTime = startTimeAtInstant,
            sid = recordingSid,
            source = Recording.Source.StartCallRecordingAPI,
            status = Recording.Status.InProgress,
            track = Some(Recording.Track.Both)
          )

        val resultFut: Future[
          Either[
            CallRecordingCreateRequestExecutor.CallRecordingCreateException,
            Recording
          ]
        ] = instance.run(connSettings, req)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "return a Left if the call does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseCallNotFound)
            )
        )

        val resultFut: Future[
          Either[
            CallRecordingCreateRequestExecutor.CallRecordingCreateException,
            Recording
          ]
        ] =
          instance.run(connSettings, req)
        val expected = Left(
          CallRecordingCreateRequestExecutor.CallRecordingCreateException
            .CallNotFound(connSettings.accountSid, callSid)
        )
        resultFut.map(res => assert(res === expected))
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val resultFut: Future[
          Either[
            CallRecordingCreateRequestExecutor.CallRecordingCreateException,
            Recording
          ]
        ]            = instance.run(connSettings, req)
        val expected =
          Left(
            CallRecordingCreateRequestExecutor.CallRecordingCreateException.Api(
              ApiException.AuthenticationException()
            )
          )
        resultFut.map(res => assert(res === expected))
      }
    }
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "api_version": "2010-04-01",
      |  "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "conference_sid": null,
      |  "channels": 2,
      |  "date_created": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "date_updated": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "start_time": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "price": null,
      |  "price_unit": null,
      |  "duration": null,
      |  "sid": "REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "source": "StartCallRecordingAPI",
      |  "status": "in-progress",
      |  "error_code": null,
      |  "encryption_details": null,
      |  "track": "both",
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
      |}
      |""".stripMargin

  private def twilioResponseCallNotFound =
    """{
      |  "code": 20404,
      |  "message": "The requested resource /2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
      |  "more_info": "https://www.twilio.com/docs/errors/20404",
      |  "status": 404
      |}
      |""".stripMargin

  private def twilioResponseInvalidCredentials =
    """{
      |  "code": 20003,
      |  "detail": "Your AccountSid or AuthToken was incorrect.",
      |  "message": "Authentication Error - No credentials provided",
      |  "more_info": "https://www.twilio.com/docs/errors/20003",
      |  "status": 401
      |}
      |""".stripMargin

  // noinspection TypeAnnotation
  final class Fixture {

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val callSid      = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val recordingSid = Recording.Sid.unsafe("REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val req          =
      CallRecordingCreateRequestExecutor.CallRecordingCreateRequest.build(
        _.withAccountSid(connSettings.accountSid)
          .withCallSid(callSid)
          .withRecordingStatusCallback(CallbackUrl("https://myapp.com/recording-events"))
          .withRecordingStatusCallbackEvent(
            Set(Recording.CallbackStatus.InProgress, Recording.CallbackStatus.Completed)
          )
          .withRecordingChannels(Recording.RecordingChannels.Dual)
          .build()
      )

    val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    val startTimeAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls/$callSid/Recordings.json"
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("RecordingStatusCallback", "utf-8")}=${URLEncoder
              .encode("https://myapp.com/recording-events", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("RecordingStatusCallbackEvent", "utf-8")}=${URLEncoder
              .encode("in-progress completed", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(s"""${URLEncoder.encode("RecordingChannels", "utf-8")}=${URLEncoder
            .encode("dual", "utf-8")}""")
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val instance: CallRecordingCreateRequestExecutor =
      TwilioClient.defaultImpl().voice.callRecordingCreate
  }
}
