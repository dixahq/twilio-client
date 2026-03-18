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
import com.dixa.twilio.client.voice.{ConferenceReadRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.{Iso8601DateTime, PublicEdgeLocation}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo}
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import org.scalatest.matchers.should.Matchers

import java.time._
import java.time.temporal.ChronoUnit
import java.util.{HashMap => JavaMap}

final class ConferenceReadTest extends TwilioClientTest with Matchers {

  import ConferenceReadTest._

  classOf[TwilioClientVoice].getSimpleName when {

    val connectionSettings = connSettings(wireMockServer.port())
    val accountSid         = connectionSettings.accountSid

    "conferenceRead" should {

      "no conferences should turn into an empty list" in {

        val returnedBody = conferenceListResp(accountSid, List.empty)

        val expectedPath = s"/2010-04-01/Accounts/$accountSid/Conferences.json"

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
        val req      = ConferenceReadRequestExecutor.ConferenceReadRequest.build(
          _.withAccountSid(accountSid)
            .build()
        )
        val result =
          instance.conferenceRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result shouldBe Seq.empty
          result.isEmpty shouldBe true
        }
      }

      "lists a single conference" in {

        val expectedQueryParam = new JavaMap[String, StringValuePattern]()
        expectedQueryParam.put("AccountSid%3E", equalTo(accountSid.toString))

        val expected = conference(
          accountSid,
          Conference.Status.Completed,
          PublicEdgeLocation.Dublin,
          Conference.FriendlyName("testConferenece"),
          Some(Conference.EndReason.ConferenceEndedViaApi)
        )

        val expectedPath = s"/2010-04-01/Accounts/$accountSid/Conferences.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(expectedPath)
            )
            .withBasicAuth(connectionSettings.accountSid.toString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withBody(conferenceListResp(accountSid, List(expected)))
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      = ConferenceReadRequestExecutor.ConferenceReadRequest.build(
          _.withAccountSid(accountSid)
            .build()
        )
        val result =
          instance.conferenceRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 1
          result.head.isRight shouldBe true
          result.head shouldBe Right(expected)
          result.head.map { _.dateCreated.toString } shouldBe Right("2020-07-01T11:23:45Z")
          result.head.map { _.dateUpdated.toString } shouldBe Right("2020-07-01T11:23:45Z")
        }
      }

      "lists multiple of conferences" in {
        val expected = conference(
          accountSid,
          Conference.Status.Completed,
          PublicEdgeLocation.Dublin,
          Conference.FriendlyName("testConference"),
          Some(Conference.EndReason.ConferenceEndedViaApi)
        )
        val expected2 = conference(
          accountSid,
          Conference.Status.InProgress,
          PublicEdgeLocation.SaoPaulo,
          Conference.FriendlyName("testConference2"),
          Some(Conference.EndReason.LastParticipantLeft)
        )
        val expected3 = conference(
          accountSid,
          Conference.Status.Init,
          PublicEdgeLocation.Ashburn,
          Conference.FriendlyName("testConference3"),
          Some(Conference.EndReason.ParticipantWithEndConferenceOnExitLeft)
        )

        val expectedPath = s"/2010-04-01/Accounts/$accountSid/Conferences.json"

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
                  conferenceListResp(
                    connectionSettings.accountSid,
                    List(expected, expected2, expected3)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      = ConferenceReadRequestExecutor.ConferenceReadRequest.build(
          _.withAccountSid(accountSid)
            .build()
        )
        val result =
          instance.conferenceRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 3
          result should contain theSameElementsAs List(expected, expected2, expected3).map {
            Right(_)
          }
        }
      }

      "lists multiple of confereces, with filter parameters" in {
        val expected2 = conference(
          accountSid,
          Conference.Status.InProgress,
          PublicEdgeLocation.SaoPaulo,
          Conference.FriendlyName("testConference2"),
          Some(Conference.EndReason.LastParticipantLeft)
        )

        val expectedPath =
          s"/2010-04-01/Accounts/$accountSid/Conferences.json?" +
            s"Status=in-progress&" +
            s"FriendlyName=testConference2&" +
            s"DateUpdated%3C=2020-07-31T11%3A23%3A45Z&" +
            s"DateCreated%3C=2020-07-31T11%3A23%3A45Z"

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
                  conferenceListResp(
                    connectionSettings.accountSid,
                    List(expected2)
                  )
                )
            )
        )

        val instance = TwilioClient.defaultImpl().voice
        val req      = ConferenceReadRequestExecutor.ConferenceReadRequest.build(
          _.withAccountSid(accountSid)
            .withDateCreated(Iso8601DateTime.Before(createdAtInstant.plus(30, ChronoUnit.DAYS)))
            .withDateUpdated(Iso8601DateTime.Before(updatedAtInstant.plus(30, ChronoUnit.DAYS)))
            .withFriendlyName(Conference.FriendlyName("testConference2"))
            .withStatus(Conference.Status.InProgress)
            .build()
        )
        val result =
          instance.conferenceRead.source(connectionSettings, req).runWith(Sink.seq)
        result.map { result =>
          result.size shouldBe 1
          result should contain theSameElementsAs List(expected2).map {
            Right(_)
          }
        }
      }
    }
  }
}

private object ConferenceReadTest {
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

  private val callSidEndingConference: Option[Call.Sid] =
    Some(Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
  private def conference(
      accountSid: TwilioAccount.Sid,
      status: Conference.Status,
      edgeLocation: PublicEdgeLocation,
      friendlyName: Conference.FriendlyName,
      reason: Option[Conference.EndReason]
  ) = Conference.DefaultImpl(
    sid = conferenceSid,
    status = status,
    friendlyName = friendlyName,
    accountSid = accountSid,
    dateCreated = createdAtInstant,
    dateUpdated = updatedAtInstant,
    edgeLocation = edgeLocation,
    reasonConferenceEnded = reason,
    callSidEndingConference = callSidEndingConference,
  )

  def conferenceReferenceResp(
      accountSid: TwilioAccount.Sid,
      status: Conference.Status,
      edgeLocation: PublicEdgeLocation,
      friendlyName: Conference.FriendlyName,
      reason: Option[Conference.EndReason]
  ): String = {
    s"""{
       |  "status": "${status.twilioString}",
       |  "region": "${edgeLocation.legacyRegionId.map(_.twilioString).get}",
       |  "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "date_updated": "Wed, 01 Jul 2020 11:23:45 +0000",
       |  "date_created": "Wed, 01 Jul 2020 11:23:45 +0000",
       |  "subresource_uris": {
       |    "participants": "/2010-04-01/Accounts/$accountSid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Participants.json",
       |    "recordings": "/2010-04-01/Accounts/$accountSid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings.json"
       |  },
       |  "friendly_name": "${friendlyName}",
       |  "uri": "/2010-04-01/Accounts/$accountSid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |  "api_version": "2010-04-01",
       |  "account_sid": "$accountSid",
       |  "reason_conference_ended": ${reason
        .map(r => s""""${r.twilioString}"""")
        .getOrElse("null")},
       |  "call_sid_ending_conference": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
       |}""".stripMargin
  }

  private def conferenceListResp(
      accountSid: TwilioAccount.Sid,
      conferences: List[Conference]
  ): String =
    s"""{
       |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Conferences.json?PageSize=1000&Page=0",
       |    "end": 0,
       |    "conferences": [
       |         ${conferences
        .map(conf =>
          conferenceReferenceResp(
            accountSid,
            conf.status,
            conf.edgeLocation,
            conf.friendlyName,
            conf.reasonConferenceEnded
          )
        )
        .mkString(", ")}
       |    ],
       |    "previous_page_uri": null,
       |    "uri": "/2010-04-01/Accounts/$accountSid/Conferences.json?PageSize=1000&Page=0",
       |    "page_size": 1000,
       |    "start": 0,
       |    "next_page_uri": null,
       |    "page": 0
       |}
       |""".stripMargin
}
