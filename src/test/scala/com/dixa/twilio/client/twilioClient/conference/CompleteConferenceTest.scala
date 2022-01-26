package com.dixa.twilio.client.twilioClient.conference

import com.dixa.twilio.client.model.TwilioAccount.{Name, Status}
import com.dixa.twilio.client.model.{TwilioAccount, TwilioConference}
import com.dixa.twilio.client.twilioClient.{TwilioClientTest, WireMockTest}
import com.dixa.twilio.client.{
  twilioClient,
  TestActorSystem,
  TwilioClient,
  TwilioClientConference,
  TwilioTestConstants
}
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

final class CompleteConferenceTest extends TwilioClientTest {

  private val account1 = TwilioAccount(
    Name("Test Account 1"),
    TwilioAccount.Sid("TwilioTestAccount1"),
    Status.Active
  )

  private val conference1 = TwilioConference(
    sid = TwilioConference.Sid("TwilioTestConference1Sid"),
    status = TwilioConference.Status.InProgress,
    friendlyName = TwilioConference.FriendlyName("Conference1FriendlyName"),
    accountSid = account1.sid
  )

  private val twilioCompleteConferenceResponseJson =
    """{
      |  "status": "completed",
      |  "reason_conference_ended": "conference-ended-via-api",
      |  "date_updated": "Wed, 06 Oct 2021 16:02:10 +0000",
      |  "region": "ie1",
      |  "friendly_name": "Conference1FriendlyName",
      |  "uri": "/2010-04-01/Accouts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid.json",
      |  "account_sid": "TwilioTestAccount1",
      |  "call_sid_ending_conference": null,
      |  "sid": "TwilioTestConference1Sid",
      |  "date_created": "Wed, 06 Oct 2021 15:55:00 +0000",
      |  "api_version": "2010-04-01",
      |  "subresource_uris": {
      |    "participants": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Participants.json",
      |    "recordings": "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid/Recordings.json"
      |  }
      |}
      |""".stripMargin

  classOf[TwilioClient].getSimpleName when {

    "ask complete a conference" should {
      "tell twilio to complete it, and return the new completed conference" in {

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/TwilioTestAccount1/Conferences/TwilioTestConference1Sid.json"
              )
            )
            .withRequestBody(WireMock.equalTo("Status=completed"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioCompleteConferenceResponseJson)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientConference = TwilioClient.defaultImpl().conference
        val resultFut: Future[TwilioConference] =
          instance.completeConference(connSettings, conference1)
        val result = Await.result(resultFut, 15.seconds)
        val expectedValue = conference1.copy(
          status = TwilioConference.Status.Completed
        )
        assert(result === expectedValue)
      }
    }
  }

}
