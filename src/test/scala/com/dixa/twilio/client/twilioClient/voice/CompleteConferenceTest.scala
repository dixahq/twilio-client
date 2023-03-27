package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{ConferenceUpdateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.PublicEdgeLocation
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneOffset}
import scala.concurrent.Future

final class CompleteConferenceTest extends TwilioClientTest {

  private val account1Sid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

  private val createdAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2021, 10, 6), LocalTime.of(15, 55, 0)),
      ZoneOffset.UTC
    )
  )
  private val updatedAtInstant = Instant.from(
    OffsetDateTime.of(
      LocalDateTime.of(LocalDate.of(2021, 10, 6), LocalTime.of(16, 2, 10)),
      ZoneOffset.UTC
    )
  )

  private val conference1 = Conference(
    sid = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
    status = Conference.Status.InProgress,
    friendlyName = Conference.FriendlyName("Conference1FriendlyName"),
    accountSid = account1Sid,
    dateCreated = createdAtInstant,
    dateUpdated = updatedAtInstant,
    edgeLocation = PublicEdgeLocation.Dublin,
    reasonConferenceEnded = Some(Conference.EndReason.ConferenceEndedViaApi),
    callSidEndingConference = None
  )

  private val twilioCompleteConferenceResponseJson =
    """{
      |  "status": "completed",
      |  "reason_conference_ended": "conference-ended-via-api",
      |  "date_updated": "Wed, 06 Oct 2021 16:02:10 +0000",
      |  "region": "ie1",
      |  "friendly_name": "Conference1FriendlyName",
      |  "uri": "/2010-04-01/Accouts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json",
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "call_sid_ending_conference": null,
      |  "sid": "CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |  "date_created": "Wed, 06 Oct 2021 15:55:00 +0000",
      |  "api_version": "2010-04-01",
      |  "subresource_uris": {
      |    "participants": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Participants.json",
      |    "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1/Recordings.json"
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
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conferences/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json"
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

        val connSettings                = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientVoice = TwilioClient.defaultImpl().voice
        val req = ConferenceUpdateRequestExecutor.ConferenceUpdateRequest.builder { builder =>
          builder
            .withAccountSid(account1Sid)
            .withConferenceSid(conference1.sid)
            .withStatus(Conference.Status.Completed)
            .build()
        }
        val resultFut: Future[
          Either[ConferenceUpdateRequestExecutor.ConferenceUpdateException, Conference]
        ] =
          instance.conferenceUpdate.run(connSettings, req)
        val expectedValue = conference1.copy(
          status = Conference.Status.Completed
        )
        resultFut.map(result => assert(result === Right(expectedValue)))
      }
    }
  }

}
