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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.TwilioClientVoice
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.PublicEdgeLocation
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

final class FetchAllConferencesForAccountsTest extends TwilioClientTest {

  import FetchAllConferencesForAccountsTest._

  classOf[TwilioClient].getSimpleName when {

    "ask to fetch all conferences for accounts" should {
      "return a flow of all the in progress conferences " in {

        // Setup wiremock to emulate fetching conferences from the two differenc accounts
        // As Twilio is using paging, we emulate 3 request, one for each account, but for
        // the first account, the request only returns some of the elements, and leaves
        // a link for the fetching the next onces.
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$account1Sid/Conferences.json"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withQueryParam("Status", WireMock.equalTo("in-progress"))
            .willReturn(
              WireMock.aResponse
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getConferencesAccount1Response1)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$account1Sid/Conferences.json"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withQueryParam("Status", WireMock.equalTo("in-progress"))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam("PageToken", WireMock.equalTo("PACFda6b2b3527379329c1394829dfb9768e"))
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getConferencesAccount1Response2)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/$account2Sid/Conferences.json"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withQueryParam("Status", WireMock.equalTo("in-progress"))
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getConferencesAccount2Response1)
            )
        )

        // Each conference just leaves a link to fetch participant data, so we need
        // to emulate these participants calls in twilio to. Here it would be a call
        // for each conference, but again twilio is using paging, so for the first
        // conference, we emulate that the first request only returns some of the
        // partitipants, and a link for fetching the next onces.
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences1Response1)
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam("PageToken", WireMock.equalTo("soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo"))
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences1Response2)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences2Response)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences3Response)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences4Response)
            )
        )

        val twilioConnectionSetting     = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientVoice = TwilioClient.defaultImpl().voice

        val resultFlow: Flow[TwilioAccount.Sid, ConferenceWithParticipants, NotUsed] =
          instance.fetchAllConferencesWithParticipants(
            twilioConnectionSetting,
            statusFilter = Some(Conference.Status.InProgress)
          )
        val resultFut: Future[Seq[ConferenceWithParticipants]] =
          Source(List(account1Sid, account2Sid)).via(resultFlow).toMat(Sink.seq)(Keep.right).run()

        val expectedValue = Set(
          // 3 conferences for the 1. account (so that we can test pagination
          ConferenceWithParticipants(
            sid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference1FriendlyName"),
            accountSid = account1Sid,
            dateCreated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
            ),
            dateUpdated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
            ),
            edgeLocation = PublicEdgeLocation.Ashburn,
            reasonConferenceEnded = None,
            callSidEndingConference = None,
            participants = Vector(
              // 3 participants in this conference, so we can test pagination of fetching participants
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X1"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = true,
                muted = false,
                hold = false,
                startConferenceOnEnter = true,
                status = Conference.Participant.Status.Connected,
              ),
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X2"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = false,
                muted = false,
                hold = false,
                startConferenceOnEnter = false,
                status = Conference.Participant.Status.Connected
              ),
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X3"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = true,
                muted = false,
                hold = false,
                startConferenceOnEnter = true,
                status = Conference.Participant.Status.Connected
              )
            )
          ),
          ConferenceWithParticipants(
            sid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference2FriendlyName"),
            accountSid = account1Sid,
            dateCreated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
            ),
            dateUpdated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
            ),
            edgeLocation = PublicEdgeLocation.Ashburn,
            reasonConferenceEnded = None,
            callSidEndingConference = None,
            participants = Vector(
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X1"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = true,
                muted = false,
                hold = false,
                startConferenceOnEnter = true,
                status = Conference.Participant.Status.Connected
              ),
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X2"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = false,
                muted = false,
                hold = false,
                startConferenceOnEnter = false,
                status = Conference.Participant.Status.Connected
              )
            )
          ),
          ConferenceWithParticipants(
            sid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference3FriendlyName"),
            accountSid = account1Sid,
            dateCreated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
            ),
            dateUpdated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
            ),
            edgeLocation = PublicEdgeLocation.Ashburn,
            reasonConferenceEnded = None,
            callSidEndingConference = None,
            participants = Vector(
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X1"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = true,
                muted = false,
                hold = false,
                startConferenceOnEnter = true,
                status = Conference.Participant.Status.Connected
              ),
              Conference.Participant(
                accountSid = account1Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X2"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = false,
                muted = false,
                hold = false,
                startConferenceOnEnter = false,
                status = Conference.Participant.Status.Connected
              )
            )
          ),
          // One conference for the second account
          ConferenceWithParticipants(
            sid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference4FriendlyName"),
            accountSid = account2Sid,
            dateCreated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
            ),
            dateUpdated = Instant.from(
              DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
            ),
            edgeLocation = PublicEdgeLocation.Ashburn,
            reasonConferenceEnded = None,
            callSidEndingConference = None,
            participants = Vector(
              Conference.Participant(
                accountSid = account2Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = true,
                muted = false,
                hold = false,
                startConferenceOnEnter = true,
                status = Conference.Participant.Status.Connected
              ),
              Conference.Participant(
                accountSid = account2Sid,
                callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2"),
                label = None,
                callSidToCoach = None,
                coaching = false,
                conferenceSid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4"),
                dateCreated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:42 +0000")
                ),
                dateUpdated = Instant.from(
                  DateTimeFormatter.RFC_1123_DATE_TIME.parse("Thu, 30 Sep 2021 06:30:46 +0000")
                ),
                endConferenceOnExit = false,
                muted = false,
                hold = false,
                startConferenceOnEnter = false,
                status = Conference.Participant.Status.Connected
              )
            )
          )
        )
        resultFut.map(result => assert(result.toSet === expectedValue))
      }
    }
  }
}

//noinspection TypeAnnotation
private object FetchAllConferencesForAccountsTest {
  val account1Sid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
  val account2Sid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")

  def getConferencesAccount1Response1 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 1,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference1FriendlyName",
      |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |      "call_sid_ending_conference": null,
      |      "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Recordings.json"
      |      }
      |    },
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference2FriendlyName",
      |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2.json",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |      "call_sid_ending_conference": null,
      |      "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Conferences.json?Status=in-progress&PageSize=20&Page=0",
      |  "page_size": 2,
      |  "start": 0,
      |  "next_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences.json?Status=in-progress&PageSize=2&Page=1&PageToken=PACFda6b2b3527379329c1394829dfb9768e",
      |  "page": 0
      |}
      |""".stripMargin

  def getConferencesAccount1Response2 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 2,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference3FriendlyName",
      |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3.json",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |      "call_sid_ending_conference": null,
      |      "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences.json?PageSize=2&Page=0&PageToken=PBCF413fe77c0869d36017df9dc313d4a664",
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Conferences.json?Status=in-progress&PageSize=2&Page=1",
      |  "page_size": 1,
      |  "start": 1,
      |  "next_page_uri": null,
      |  "page": 1
      |}
      |""".stripMargin

  def getConferencesAccount2Response1 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 0,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference4FriendlyName",
      |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4.json",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
      |      "call_sid_ending_conference": null,
      |      "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Conferences.json?Status=in-progress&PageSize=2&Page=1",
      |  "page_size": 1,
      |  "start": 0,
      |  "next_page_uri": null,
      |  "page": 0
      |}
      |""".stripMargin

  def getParticipantsConferences1Response1 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X1.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X1",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X2.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X2",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences1Response2 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=0",
       |  "end": 2,
       |  "previous_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=0&PageToken=AeF2eix6AW9aeshaloRaeGhahr2inee5aeL1",
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page_size": 1,
       |  "start": 1,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X3.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X3",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 1
       |}
       |""".stripMargin

  def getParticipantsConferences2Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X1.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X1",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X2.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X2",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences3Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X1.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X1",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X2.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X2",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences4Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account2Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account2Sid",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account2Sid/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "$account2Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin
}
