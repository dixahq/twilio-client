// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.voice

import org.apache.pekko.stream.scaladsl.Sink
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{ConferenceParticipantReadRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._

final class ConferenceParticipantReadTest extends TwilioClientTest with Matchers {

  import ConferenceParticipantReadTest._

  classOf[TwilioClientVoice].getSimpleName when {

    val connectionSettings = connSettings(wireMockServer.port())
    val accountSid         = connectionSettings.accountSid

    "conferenceRead" should {

      "no conferences should turn into an empty list" in {

        val returnedBody = participantsListResp(accountSid, conferenceSid, List.empty)

        val expectedPath =
          s"/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(expectedPath)
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(returnedBody)
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      =
          ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.build(
            _.withAccountSid(accountSid)
              .withConferenceSid(conferenceSid)
              .build()
          )
        val result =
          instance.conferenceParticipantsRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result shouldBe Seq.empty
          result.isEmpty shouldBe true
        }
      }

      "lists a single conference" in {

        val expected = participant(
          accountSid,
          conferenceSid,
          callSid,
          Some(Conference.Participant.Label("testing")),
          Conference.Participant.Status.Ringing
        )

        val expectedPath =
          s"/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(expectedPath)
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(participantsListResp(accountSid, conferenceSid, List(expected)))
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      =
          ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.build(
            _.withAccountSid(accountSid)
              .withConferenceSid(conferenceSid)
              .build()
          )
        val result =
          instance.conferenceParticipantsRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.head.left.map { ex =>
            ex.getStackTrace.map(println)
          }
          result.size shouldBe 1
          result.head.isRight shouldBe true
          result.head shouldBe Right(expected)
          result.head.map { _.dateCreated.toString } shouldBe Right("2020-07-01T11:23:45Z")
          result.head.map { _.dateUpdated.toString } shouldBe Right("2020-07-01T11:23:45Z")
        }
      }

      "lists multiple of conferences" in {

        val expected = participant(
          accountSid,
          conferenceSid,
          callSid,
          Some(Conference.Participant.Label("testingParticipant")),
          Conference.Participant.Status.Ringing
        )

        val expected2 = participant(
          accountSid,
          conferenceSid,
          callSid,
          Some(Conference.Participant.Label("testingParticipant2")),
          Conference.Participant.Status.Queued
        )

        val expected3 = participant(
          accountSid,
          conferenceSid,
          callSid,
          Some(Conference.Participant.Label("testingParticipant3")),
          Conference.Participant.Status.Connected
        )

        val expectedPath =
          s"/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(expectedPath)
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  participantsListResp(
                    connectionSettings.accountSid,
                    conferenceSid,
                    List(expected, expected2, expected3)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      =
          ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.build(
            _.withAccountSid(accountSid)
              .withConferenceSid(conferenceSid)
              .build()
          )
        val result =
          instance.conferenceParticipantsRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.head.left.map { ex =>
            println(ex.getMessage)
            ex.getStackTrace.map(println)
          }
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3).map {
            Right(_)
          }
        }
      }

      "lists multiple of confereces, with filter parameters" in {
        val expected2 = participant(
          accountSid,
          conferenceSid,
          callSid,
          Some(Conference.Participant.Label("testingParticipant2")),
          Conference.Participant.Status.Queued
        )

        val expectedPath =
          s"/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json?" +
            s"Coaching=false&" +
            s"Hold=false&" +
            s"Muted=false"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlEqualTo(expectedPath)
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(
                  participantsListResp(
                    connectionSettings.accountSid,
                    conferenceSid,
                    List(expected2)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      =
          ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest.build(
            _.withAccountSid(accountSid)
              .withConferenceSid(conferenceSid)
              .withMuted(false)
              .withHold(false)
              .withCoaching(false)
              .build()
          )
        val result =
          instance.conferenceParticipantsRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.head.left.map { ex =>
            println(ex.getMessage)
            ex.getStackTrace.map(println)
          }
          result.size shouldBe 1
          result should contain theSameElementsAs List(expected2).map {
            Right(_)
          }
        }
      }
    }
  }
}

private object ConferenceParticipantReadTest {
  private def connSettings(port: Int) = TwilioTestConstants.connSettings(port)

  private val createdAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2020, 7, 1), LocalTime.of(11, 23, 45)),
      ZoneOffset.UTC
    )
  )

  private val updatedAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2020, 7, 1), LocalTime.of(11, 23, 45)),
      ZoneOffset.UTC
    )
  )

  private val conferenceSid: Conference.Sid =
    Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

  private val callSid: Call.Sid =
    Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
  private def participant(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      callSid: Call.Sid,
      label: Option[Conference.Participant.Label],
      status: Conference.Participant.Status
  ) = Conference.Participant(
    accountSid = accountSid,
    conferenceSid = conferenceSid,
    callSid = callSid,
    label = label,
    callSidToCoach = None,
    coaching = false,
    dateCreated = createdAtInstant,
    dateUpdated = updatedAtInstant,
    endConferenceOnExit = false,
    muted = true,
    hold = false,
    startConferenceOnEnter = true,
    status = status
  )

  def participantResp(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      callSid: Call.Sid,
      label: Option[Conference.Participant.Label],
      status: Conference.Participant.Status
  ): String = {
    s"""{
       |  "account_sid": "$accountSid",
       |  "call_sid": "$callSid",
       |  "label": ${label.map(l => s""""$l"""").getOrElse("null")},
       |  "conference_sid": "$conferenceSid",
       |  "date_created": "Wed, 1 Jul 2020 11:23:45 +0000",
       |  "date_updated": "Wed, 1 Jul 2020 11:23:45 +0000",
       |  "end_conference_on_exit": false,
       |  "muted": true,
       |  "hold": false,
       |  "status": "${status.twilioString}",
       |  "start_conference_on_enter": true,
       |  "coaching": false
       |}""".stripMargin
  }

  private def participantsListResp(
      accountSid: TwilioAccount.Sid,
      conferenceSid: Conference.Sid,
      participants: List[Conference.Participant]
  ): String =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "participants": [
       |         ${participants
        .map(participant =>
          participantResp(
            accountSid,
            participant.conferenceSid,
            participant.callSid,
            participant.label,
            participant.status
          )
        )
        .mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Conferences/$conferenceSid/Participants.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
