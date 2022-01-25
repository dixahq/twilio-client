package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.dixa.twilio.client.model.TwilioConference.TwilioConferenceWithParticipants
import com.dixa.twilio.client.model.{TwilioAccount, TwilioCallSid, TwilioConference}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

final class TwilioClientFetchAllConferencesForAccountsTest
    extends AnyWordSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with TestActorSystem {

  import TwilioClientFetchAllConferencesForAccountsTest._

  private val wireMockServer = new WireMockServer(0)
  wireMockServer.start()

  import actorSystem.dispatcher

  override protected def beforeEach(): Unit = {
    wireMockServer.resetAll()
    super.beforeEach()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  classOf[TwilioClient].getSimpleName when {

    "ask to fetch all conferences for accounts" should {
      "return a flow of all the in progress conferences " in {

        // Setup wiremock to emulate fetching conferences from the two differenc accounts
        // As Twilio is using paging, we emulate 3 request, one for each account, but for
        // the first account, the request only returns some of the elements, and leaves
        // a link for the fetching the next onces.
        wireMockServer.stubFor(
          WireMock
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/${account1.sid}/Conferences.json"))
            .withBasicAuth("testUsername", "testPassword")
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
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/${account1.sid}/Conferences.json"))
            .withBasicAuth("testUsername", "testPassword")
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
            .get(WireMock.urlPathEqualTo(s"/2010-04-01/Accounts/${account2.sid}/Conferences.json"))
            .withBasicAuth("testUsername", "testPassword")
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
            .withBasicAuth("testUsername", "testPassword")
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
            .withBasicAuth("testUsername", "testPassword")
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
            .withBasicAuth("testUsername", "testPassword")
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
            .withBasicAuth("testUsername", "testPassword")
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
            .withBasicAuth("testUsername", "testPassword")
            .willReturn(
              WireMock
                .aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(getParticipantsConferences4Response)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientConference = TwilioClient.defaultImpl().conference

        val resultFlow: Flow[TwilioAccount, TwilioConference, NotUsed] =
          instance.fetchAllConferencesWithParticipants(
            twilioConnectionSetting,
            statusFilter = Some(TwilioConference.Status.InProgress)
          )
        val resultFut =
          Source(List(account1, account2)).via(resultFlow).toMat(Sink.seq)(Keep.right).run()
        val result = Await.result(resultFut, 15.seconds)
        val expectedValue = Set(
          // 3 conferences for the 1. account (so that we can test pagination
          TwilioConferenceWithParticipants(
            sid = TwilioConference.Sid("TwilioTestConference1Sid"),
            status = TwilioConference.Status.InProgress,
            friendlyName = TwilioConference.FriendlyName("Conference1FriendlyName"),
            accountSid = account1.sid,
            participants = Vector(
              // 3 participants in this conference, so we can test pagination of fetching participants
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference1Participant1CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              ),
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference1Participant2CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              ),
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference1Participant3CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              )
            )
          ),
          TwilioConferenceWithParticipants(
            sid = TwilioConference.Sid("TwilioTestConference2Sid"),
            status = TwilioConference.Status.InProgress,
            friendlyName = TwilioConference.FriendlyName("Conference2FriendlyName"),
            accountSid = account1.sid,
            participants = Vector(
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference2Participant1CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              ),
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference2Participant2CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              )
            )
          ),
          TwilioConferenceWithParticipants(
            sid = TwilioConference.Sid("TwilioTestConference3Sid"),
            status = TwilioConference.Status.InProgress,
            friendlyName = TwilioConference.FriendlyName("Conference3FriendlyName"),
            accountSid = account1.sid,
            participants = Vector(
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference3Participant1CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              ),
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference3Participant2CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              )
            )
          ),
          // One conference for the second account
          TwilioConferenceWithParticipants(
            sid = TwilioConference.Sid("TwilioTestConference4Sid"),
            status = TwilioConference.Status.InProgress,
            friendlyName = TwilioConference.FriendlyName("Conference4FriendlyName"),
            accountSid = account2.sid,
            participants = Vector(
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference4Participant1CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              ),
              TwilioConference.Participant(
                callSid = TwilioCallSid("TestConference4Participant2CallSid"),
                status = TwilioConference.ParticipantStatus.Connected
              )
            )
          )
        )
        assert(result.toSet === expectedValue)
      }
    }
  }
}

//noinspection TypeAnnotation
private object TwilioClientFetchAllConferencesForAccountsTest {
  val account1 = TwilioAccount(
    TwilioAccount.Name("Test Account 1"),
    TwilioAccount.Sid("TwilioTestAccount1"),
    TwilioAccount.Status.Active
  )
  val account2 = TwilioAccount(
    TwilioAccount.Name("Test Account 2"),
    TwilioAccount.Sid("TwilioTestAccount2"),
    TwilioAccount.Status.Active
  )

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
       |  "first_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants/TestConference1Participant1CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "TestConference1Participant1CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants/TestConference1Participant2CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "TestConference1Participant2CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences1Response2 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 2,
       |  "previous_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=0&PageToken=AeF2eix6AW9aeshaloRaeGhahr2inee5aeL1",
       |  "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants.json?PageSize=2&Page=1&PageToken=soo2ei1aiv0Ohvahk0aingeeSh0eet1taivo",
       |  "page_size": 1,
       |  "start": 1,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference1Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference1Sid/Participants/TestConference1Participant3CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "TestConference1Participant3CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 1
       |}
       |""".stripMargin

  def getParticipantsConferences2Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference2Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference2Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference2Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference2Sid/Participants/TestConference2Participant1CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "TestConference2Participant1CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference2Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference2Sid/Participants/TestConference2Participant2CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "TestConference2Participant2CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences3Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference3Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference3Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference3Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference3Sid/Participants/TestConference3Participant1CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "TestConference3Participant1CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference3Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference3Sid/Participants/TestConference3Participant2CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "TestConference3Participant2CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin

  def getParticipantsConferences4Response =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference4Sid/Participants.json?PageSize=2&Page=0",
       |  "end": 1,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference4Sid/Participants.json?PageSize=2&Page=0",
       |  "page_size": 2,
       |  "start": 0,
       |  "participants": [
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference4Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": true,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference4Sid/Participants/TestConference4Participant1CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": true,
       |      "call_sid": "TestConference4Participant1CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    },
       |    {
       |      "status": "connected",
       |      "conference_sid": "TwilioTestConference4Sid",
       |      "hold": false,
       |      "date_updated": "Thu, 30 Sep 2021 06:30:46 +0000",
       |      "end_conference_on_exit": false,
       |      "uri": "/2010-04-01/Accounts/${account1.sid}/Conferences/TwilioTestConference4Sid/Participants/TestConference4Participant2CallSid.json",
       |      "label": null,
       |      "muted": false,
       |      "coaching": false,
       |      "start_conference_on_enter": false,
       |      "call_sid": "TestConference4Participant2CallSid",
       |      "date_created": "Thu, 30 Sep 2021 06:30:42 +0000",
       |      "account_sid": "${account1.sid}",
       |      "call_sid_to_coach": null
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "page": 0
       |}
       |""".stripMargin
}
