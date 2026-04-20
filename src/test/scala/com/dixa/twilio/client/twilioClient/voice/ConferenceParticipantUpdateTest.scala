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
import com.dixa.twilio.client.voice.ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateException
import com.dixa.twilio.client.voice.{ConferenceParticipantUpdateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time._
import scala.concurrent.Future

final class ConferenceParticipantUpdateTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to update a conference participant" should {

      "Support putting the participant on hold by call sid" in {

        val f = new Fixture
        import f._

        val request =
          ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest.build(
            _.withAccountSid(TwilioTestConstants.accountSid)
              .withConferenceSid(conferenceSid)
              .withCallSid(callSid)
              .withHoldTrue()
              .build()
          )

        val twilioReturn = s"""{
                              |  "account_sid": "${TwilioTestConstants.accountSid}",
                              |  "call_sid": "$callSid",
                              |  "label": null,
                              |  "conference_sid": "$conferenceSid",
                              |  "date_created": "Wed, 1 Jul 2020 11:23:45 +0000",
                              |  "date_updated": "Wed, 1 Jul 2020 12:23:45 +0000",
                              |  "end_conference_on_exit": false,
                              |  "muted": true,
                              |  "hold": false,
                              |  "status": "connected",
                              |  "start_conference_on_enter": true,
                              |  "coaching": false
                              |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Participants/$callSid.json"
              )
            )
            .andMatching(postParamMatcher(Map("Hold" -> "true")))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioReturn)
            )
        )

        val expected = Conference.Participant(
          accountSid = TwilioTestConstants.accountSid,
          conferenceSid = conferenceSid,
          callSid = callSid,
          label = None,
          callSidToCoach = None,
          coaching = false,
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          endConferenceOnExit = false,
          muted = true,
          hold = false,
          startConferenceOnEnter = true,
          status = Conference.Participant.Status.Connected
        )

        val resultFut: Future[
          Either[ConferenceParticipantUpdateException, Conference.Participant]
        ] = instance.run(connSettings, request)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "Support putting the participant on hold by label" in {

        val f = new Fixture
        import f._

        val label = Conference.Participant.Label("testLabel")

        val request =
          ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest.build(
            _.withAccountSid(TwilioTestConstants.accountSid)
              .withConferenceSid(conferenceSid)
              .withLabel(label)
              .withHoldTrue()
              .withHoldUrl(CallbackUrl("http://localhost/test"))
              .withHoldMethod(HttpMethod.Post)
              .build()
          )

        val twilioReturn =
          s"""{
             |  "account_sid": "${TwilioTestConstants.accountSid}",
             |  "call_sid": "$callSid",
             |  "label": "testLabel",
             |  "conference_sid": "$conferenceSid",
             |  "date_created": "Wed, 1 Jul 2020 11:23:45 +0000",
             |  "date_updated": "Wed, 1 Jul 2020 12:23:45 +0000",
             |  "end_conference_on_exit": false,
             |  "muted": true,
             |  "hold": false,
             |  "status": "connected",
             |  "start_conference_on_enter": true,
             |  "coaching": false
             |}""".stripMargin

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Participants/testLabel.json"
              )
            )
            .andMatching(
              postParamMatcher(
                Map("Hold" -> "true", "HoldUrl" -> "http://localhost/test", "HoldMethod" -> "POST")
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioReturn)
            )
        )

        val expected = Conference.Participant(
          accountSid = TwilioTestConstants.accountSid,
          conferenceSid = conferenceSid,
          callSid = callSid,
          label = Some(label),
          callSidToCoach = None,
          coaching = false,
          dateCreated = createdAtInstant,
          dateUpdated = updatedAtInstant,
          endConferenceOnExit = false,
          muted = true,
          hold = false,
          startConferenceOnEnter = true,
          status = Conference.Participant.Status.Connected
        )

        val resultFut: Future[
          Either[ConferenceParticipantUpdateException, Conference.Participant]
        ] = instance.run(connSettings, request)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "return a Left if the resource does not exists" in {
        val f = new Fixture

        import f._

        val expectedPath =
          s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Participants/$callSid.json"
        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                expectedPath
              )
            )
            .andMatching(postParamMatcher(Map("Muted" -> "false")))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  s"""{"code": 20404, "message": "The requested resource $expectedPath was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val request =
          ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest.build(
            _.withAccountSid(TwilioTestConstants.accountSid)
              .withConferenceSid(conferenceSid)
              .withCallSid(callSid)
              .withMuted(false)
              .build()
          )

        val resultFut: Future[
          Either[ConferenceParticipantUpdateException, Conference.Participant]
        ] =
          instance.run(connSettings, request)
        val expected = Left(
          ConferenceParticipantUpdateException.ParticipantNotFound(
            TwilioTestConstants.accountSid,
            conferenceSid,
            Left(callSid)
          )
        )
        resultFut.map(res => assert(res === expected))
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Conferences/$conferenceSid/Participants/$callSid.json"
              )
            )
            .andMatching(postParamMatcher(Map("Muted" -> "true")))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code":20003,"message":"Authenticate","more_info":"https://www.twilio.com/docs/errors/20003","status":401}"""
                )
            )
        )

        val request =
          ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest.build(
            _.withAccountSid(TwilioTestConstants.accountSid)
              .withConferenceSid(conferenceSid)
              .withCallSid(callSid)
              .withMuted(true)
              .build()
          )

        val resultFut: Future[
          Either[ConferenceParticipantUpdateException, Conference.Participant]
        ]            = instance.run(connSettings, request)
        val expected =
          Left(ConferenceParticipantUpdateException.Api(ApiException.AuthenticationException()))
        resultFut.map(res => assert(res === expected))
      }

      "Fail compile time, if you try to build a request with both a callSid and a label" in {
        assertTypeError(
          """val f = new Fixture
            |import f._
            |val label = Conference.Participant.Label("testLabel")
            |ConferenceParticipantUpdateRequestExecutor.ConferenceParticipantUpdateRequest.build(
            |          _.withAccountSid(TwilioTestConstants.accountSid)
            |            .withConferenceSid(conferenceSid)
            |            .withLabel(label)
            |            .withCallSid(callSid)
            |            .withStatus(Call.StatusUpdate.Completed)
            |            .build()
            |        )""".stripMargin
        )
      }

      "Fail to compile if you try to build a request without either call sid or label" in {
        assertTypeError("""val f = new Fixture
                          |        import f._
                          |
                          |ConferenceParticipantUpdateRequest.build(
                          |          _.withAccountSid(TwilioTestConstants.accountSid)
                          |            .withConferenceSid(conferenceSid)
                          |            .build()
                          |        )""".stripMargin)
      }

      "Fail to compile if you try to supply an hold url, without setting the call on hold" in {
        assertTypeError("""val f = new Fixture
                          |import f._
                          |
                          |ConferenceParticipantUpdateRequest.build(
                          |          _.withAccountSid(TwilioTestConstants.accountSid)
                          |            .withConferenceSid(conferenceSid)
                          |            .withCallSid(callSid)
                          |            .withHoldUrl(CallbackUrl("http://example.com"))
                          |            .build()
                          |        )""".stripMargin)
      }

      "Fail to compile if you try to call both withHoldFalse after you have called withHoldUrl" in {
        assertTypeError("""val f = new Fixture
                          |import f._
                          |ConferenceParticipantUpdateRequest.build(
                          |  _.withAccountSid(TwilioTestConstants.accountSid)
                          |    .withConferenceSid(conferenceSid)
                          |    .withCallSid(callSid)
                          |    .withHoldTrue()
                          |    .withHoldUrl(CallbackUrl("http://example.com"))
                          |    .withHoldFalse()
                          |    .build()
                          |)""".stripMargin)
      }

      "Fail to compile if hold method is provided without hold url being called first" in {
        assertTypeError("""val f = new Fixture
                          |import f._
                          |ConferenceParticipantUpdateRequest.build(
                          |  _.withAccountSid(TwilioTestConstants.accountSid)
                          |    .withConferenceSid(conferenceSid)
                          |    .withCallSid(callSid)
                          |    .withHoldUrl(CallbackUrl("http://example.com"))
                          |    .build()
                          |)""".stripMargin)
      }

      "Fail to compile if announcement method is called without an announcement url being set first" in {
        assertTypeError("""val f = new Fixture
                          |import f._
                          |ConferenceParticipantUpdateRequest.build(
                          |  _.withAccountSid(TwilioTestConstants.accountSid)
                          |    .withConferenceSid(conferenceSid)
                          |    .withCallSid(callSid)
                          |    .withAnnounceMethod(HttpMethod.Post)
                          |    .build()
                          |)""".stripMargin)
      }

      "Fail to compile if wait method is provided without a wait url" in {
        assertTypeError("""val f = new Fixture
                          |import f._
                          |ConferenceParticipantUpdateRequest.build(
                          |  _.withAccountSid(TwilioTestConstants.accountSid)
                          |    .withConferenceSid(conferenceSid)
                          |    .withCallSid(callSid)
                          |    .withWaitMethod(HttpMethod.Post)
                          |    .build()
                          |)""".stripMargin)
      }

    }
  }

  // noinspection TypeAnnotation
  final class Fixture {

    val connSettings  = TwilioTestConstants.connSettings(wireMockServer.port())
    val conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val callSid       = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

    val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2020, 7, 1), LocalTime.of(11, 23, 45)),
        ZoneOffset.UTC
      )
    )

    val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2020, 7, 1), LocalTime.of(12, 23, 45)),
        ZoneOffset.UTC
      )
    )

    val instance: ConferenceParticipantUpdateRequestExecutor =
      TwilioClient.defaultImpl().voice.conferenceParticipantUpdate
  }
}
