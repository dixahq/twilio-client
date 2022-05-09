package com.dixa.twilio.client.twilioClient.voice

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.TwilioClientVoice
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.ConferenceWithParticipants
import com.dixa.twilio.model.voice.{Conference, TwilioCallSid}
import com.github.tomakehurst.wiremock.client.WireMock

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
                "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Participants.json"
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
                "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Participants.json"
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
                "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference2Sid/Participants.json"
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
                "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference3Sid/Participants.json"
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
                "/2010-04-01/Accounts/TwilioTestAccount2/Conferences/TwilioTestConference4Sid/Participants.json"
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

        val resultFlow: Flow[TwilioAccount.Sid, Conference, NotUsed] =
          instance.fetchAllConferencesWithParticipants(
            twilioConnectionSetting,
            statusFilter = Some(Conference.Status.InProgress)
          )
        val resultFut =
          Source(List(account1Sid, account2Sid)).via(resultFlow).toMat(Sink.seq)(Keep.right).run()

        val expectedValue = Set(
          // 3 conferences for the 1. account (so that we can test pagination
          ConferenceWithParticipants(
            sid = Conference.Sid("TwilioTestConference1Sid"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference1FriendlyName"),
            accountSid = account1Sid,
            participants = Vector(
              // 3 participants in this conference, so we can test pagination of fetching participants
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X1"),
                status = Conference.ParticipantStatus.Connected
              ),
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X2"),
                status = Conference.ParticipantStatus.Connected
              ),
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X3"),
                status = Conference.ParticipantStatus.Connected
              )
            )
          ),
          ConferenceWithParticipants(
            sid = Conference.Sid("TwilioTestConference2Sid"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference2FriendlyName"),
            accountSid = account1Sid,
            participants = Vector(
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X1"),
                status = Conference.ParticipantStatus.Connected
              ),
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X2"),
                status = Conference.ParticipantStatus.Connected
              )
            )
          ),
          ConferenceWithParticipants(
            sid = Conference.Sid("TwilioTestConference3Sid"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference3FriendlyName"),
            accountSid = account1Sid,
            participants = Vector(
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X1"),
                status = Conference.ParticipantStatus.Connected
              ),
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X2"),
                status = Conference.ParticipantStatus.Connected
              )
            )
          ),
          // One conference for the second account
          ConferenceWithParticipants(
            sid = Conference.Sid("TwilioTestConference4Sid"),
            status = Conference.Status.InProgress,
            friendlyName = Conference.FriendlyName("Conference4FriendlyName"),
            accountSid = account2Sid,
            participants = Vector(
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1"),
                status = Conference.ParticipantStatus.Connected
              ),
              Conference.Participant(
                callSid = TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2"),
                status = Conference.ParticipantStatus.Connected
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
  val account1Sid = TwilioAccount.Sid("TwilioTestAccount1")
  val account2Sid = TwilioAccount.Sid("TwilioTestAccount2")

  def getConferencesAccount1Response1 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 1,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference1FriendlyName",
      |      "uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid.json",
      |      "account_sid": "TwilioTestAccount1",
      |      "call_sid_ending_conference": null,
      |      "sid": "TwilioTestConference1Sid",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Recordings.json"
      |      }
      |    },
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference2FriendlyName",
      |      "uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference2Sid.json",
      |      "account_sid": "TwilioTestAccount1",
      |      "call_sid_ending_conference": null,
      |      "sid": "TwilioTestConference2Sid",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference2Sid/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference2Sid/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Conferences.json?Status=in-progress&PageSize=20&Page=0",
      |  "page_size": 2,
      |  "start": 0,
      |  "next_page_uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences.json?Status=in-progress&PageSize=2&Page=1&PageToken=PACFda6b2b3527379329c1394829dfb9768e",
      |  "page": 0
      |}
      |""".stripMargin

  def getConferencesAccount1Response2 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 2,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference3FriendlyName",
      |      "uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference3Sid.json",
      |      "account_sid": "TwilioTestAccount1",
      |      "call_sid_ending_conference": null,
      |      "sid": "TwilioTestConference3Sid",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference3Sid/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference3Sid/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences.json?PageSize=2&Page=0&PageToken=PBCF413fe77c0869d36017df9dc313d4a664",
      |  "uri": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences.json?Status=in-progress&PageSize=2&Page=1",
      |  "page_size": 1,
      |  "start": 1,
      |  "next_page_uri": null,
      |  "page": 1
      |}
      |""".stripMargin

  def getConferencesAccount2Response1 =
    """{
      |  "first_page_uri": "/2010-04-01/Accounts/TwilioTestAccount2/Conferences.json?Status=in-progress&PageSize=2&Page=0",
      |  "end": 0,
      |  "conferences": [
      |    {
      |      "status": "in-progress",
      |      "reason_conference_ended": null,
      |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
      |      "region": "us1",
      |      "friendly_name": "Conference4FriendlyName",
      |      "uri": "/2010-04-01/Accounts/TwilioTestAccount2/Conferences/TwilioTestConference4Sid.json",
      |      "account_sid": "TwilioTestAccount2",
      |      "call_sid_ending_conference": null,
      |      "sid": "TwilioTestConference4Sid",
      |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
      |      "api_version": "2010-04-01",
      |      "subresource_uris": {
      |        "participants": "/2010-04-01/Accounts/TwilioTestAccount2/Conferences/TwilioTestConference4Sid/Participants.json",
      |        "recordings": "/2010-04-01/Accounts/TwilioTestAccount2/Conferences/TwilioTestConference4Sid/Recordings.json"
      |      }
      |    }
      |  ],
      |  "previous_page_uri": null,
      |  "uri": "/2010-04-01/Accounts/TwilioTestAccount2/Conferences.json?Status=in-progress&PageSize=2&Page=1",
      |  "page_size": 1,
      |  "start": 0,
      |  "next_page_uri": null,
      |  "page": 0
      |}
      |""".stripMargin

  def getParticipantsConferences1Response1 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X1.json",
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
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X2.json",
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
       |  "next_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences1Response2 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 2,
       |  "previous_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0&PageToken=AeF2eix6AW9aeshaloRaeGhahr2inee5aeL1",
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page_size": 1,
       |  "start": 1,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference1Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1X3.json",
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
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference2Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference2Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference2Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference2Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X1.json",
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
       |      "conference_sid": "TwilioTestConference2Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference2Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2X2.json",
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
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference3Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference3Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference3Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference3Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X1.json",
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
       |      "conference_sid": "TwilioTestConference3Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference3Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX3X2.json",
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
       |  "first_page_uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference4Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference4Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference4Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference4Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X1",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference4Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/$account1Sid/Conferences/TwilioTestConference4Sid/Participants/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXX4X2",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "$account1Sid",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin
}
