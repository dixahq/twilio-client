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
import com.dixa.twilio.client.voice.{ConferenceRecordingUpdateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{Call, Conference, Recording}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time._
import scala.concurrent.Future

final class ConferenceRecordingUpdateStatusTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to update a conference recording" should {

      "Support sending a new status to pause the conference recording without Recording Sid" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequestWithOutRecordingSid
            .withRequestBody(
              WireMock.containing(s"""${URLEncoder.encode("Status", "utf-8")}=${URLEncoder
                  .encode("paused", "utf-8")}""")
            )
            .withRequestBody(
              WireMock.containing(s"""${URLEncoder.encode("PauseBehavior", "utf-8")}=${URLEncoder
                  .encode("skip", "utf-8")}""")
            )
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected =
          Recording(
            accountSid = TwilioTestConstants.accountSid,
            callSid = callSid,
            conferenceSid = Some(conferenceSid),
            dateCreated = createdAtInstant,
            dateUpdate = updatedAtInstant,
            startTime = startTimeAtInstant,
            sid = recordingSid,
            status = Recording.Status.Paused,
            channels = Recording.Channels.unsafe(1),
            source = Recording.Source.StartCallRecordingAPI,
            track = Some(Recording.Track.Both)
          )

        val resultFut: Future[
          Either[
            ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException,
            Recording
          ]
        ] = instance.run(connSettings, pausedRequest)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "Support sending a new status to resume the call recording with Recording Sid" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .withRequestBody(
              WireMock.containing(
                s"""${URLEncoder.encode("Status", "utf-8")}=${URLEncoder
                    .encode("in-progress", "utf-8")}"""
              )
            )
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse2)
            )
        )

        val expected =
          Recording(
            accountSid = TwilioTestConstants.accountSid,
            callSid = callSid,
            conferenceSid = Some(conferenceSid),
            dateCreated = createdAtInstant,
            dateUpdate = updatedAtInstant,
            startTime = startTimeAtInstant,
            sid = recordingSid,
            status = Recording.Status.InProgress,
            channels = Recording.Channels.unsafe(1),
            source = Recording.Source.StartCallRecordingAPI,
            track = Some(Recording.Track.Both)
          )

        val resultFut: Future[
          Either[
            ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException,
            Recording
          ]
        ] = instance.run(connSettings, resumeRequest)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "return a Left if the recording does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequestWithOutRecordingSid
            .withRequestBody(
              WireMock.containing(
                s"""${URLEncoder.encode("Status", "utf-8")}=${URLEncoder
                    .encode("paused", "utf-8")}"""
              )
            )
            .withRequestBody(
              WireMock.containing(
                s"""${URLEncoder.encode("PauseBehavior", "utf-8")}=${URLEncoder
                    .encode("skip", "utf-8")}"""
              )
            )
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseCallNotFound)
            )
        )

        val resultFut: Future[
          Either[
            ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException,
            Recording
          ]
        ] =
          instance.run(connSettings, pausedRequest)
        val expected = Left(
          ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException
            .RecordingNotFound(TwilioTestConstants.accountSid, None, conferenceSid)
        )
        resultFut.map(res => assert(res === expected))
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequestWithOutRecordingSid
            .withRequestBody(
              WireMock.containing(
                s"""${URLEncoder.encode("Status", "utf-8")}=${URLEncoder
                    .encode("paused", "utf-8")}"""
              )
            )
            .withRequestBody(
              WireMock.containing(
                s"""${URLEncoder.encode("PauseBehavior", "utf-8")}=${URLEncoder
                    .encode("skip", "utf-8")}"""
              )
            )
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val resultFut: Future[
          Either[
            ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException,
            Recording
          ]
        ]            = instance.run(connSettings, pausedRequest)
        val expected =
          Left(
            ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateException.Api(
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
      |  "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "channels": 1,
      |  "date_created": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "date_updated": "Fri, 14 Oct 2016 21:56:36 +0000",
      |  "start_time": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "price": null,
      |  "price_unit": null,
      |  "duration": null,
      |  "sid": "REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "source": "StartCallRecordingAPI",
      |  "status": "paused",
      |  "error_code": null,
      |  "encryption_details": null,
      |  "track": "both",
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
      |}
      |""".stripMargin

  private def twilioResponse2 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "api_version": "2010-04-01",
      |  "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "channels": 1,
      |  "date_created": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "date_updated": "Fri, 14 Oct 2016 21:56:36 +0000",
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
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
      |}
      |""".stripMargin

  private def twilioResponseCallNotFound =
    """{
      |  "code": 20404,
      |  "message": "The requested resource /2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
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

    val connSettings  = TwilioTestConstants.connSettings(wireMockServer.port())
    val callSid       = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val recordingSid  = Recording.Sid.unsafe("REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val pausedRequest =
      ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateRequest.build(
        _.withAccountSid(TwilioTestConstants.accountSid)
          .withConferenceSid(conferenceSid)
          .withStatus(Recording.Status.Paused)
          .withPauseBehavior(Recording.PauseBehavior.Skip)
          .build()
      )

    val resumeRequest =
      ConferenceRecordingUpdateRequestExecutor.ConferenceRecordingUpdateRequest.build(
        _.withAccountSid(TwilioTestConstants.accountSid)
          .withConferenceSid(conferenceSid)
          .withSid(recordingSid)
          .withStatus(Recording.Status.InProgress)
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
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 36)),
        ZoneOffset.UTC
      )
    )

    val startTimeAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    val wireMockBuilderExpectedTwilioRequestWithOutRecordingSid = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Recordings/Twilio.CURRENT.json"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Recordings/$recordingSid.json"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val instance: ConferenceRecordingUpdateRequestExecutor =
      TwilioClient.defaultImpl().voice.conferenceRecordingUpdate
  }
}
