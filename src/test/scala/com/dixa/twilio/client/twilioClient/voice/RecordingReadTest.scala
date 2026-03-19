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

import org.apache.pekko.stream.scaladsl.Sink
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{RecordingReadRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference, Recording}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._
import scala.concurrent.Future

final class RecordingReadTest extends TwilioClientTest with Matchers {

  classOf[TwilioClientVoice].getSimpleName when {

    "read recordings" should {

      "Support reading all recording from account" in {

        val f = new Fixture
        import f._

        val expected1 = recording(
          connSettings.accountSid,
          Recording.Status.Completed,
          callSid1,
          conferenceSid1
        )

        val expected2 = recording(
          connSettings.accountSid,
          Recording.Status.Deleted,
          callSid2,
          conferenceSid2
        )

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Recordings.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlEqualTo(expectedPath)
            )
            .withBasicAuth(connSettings.accountSid.twilioString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  recordingListResp(
                    accountSid = connSettings.accountSid,
                    recordings = List(expected1, expected2)
                  )
                )
            )
        )

        val req = RecordingReadRequestExecutor.RecordingReadRequest.build(
          _.withAccountSid(connSettings.accountSid).build()
        )

        val resultFut
            : Future[Seq[Either[RecordingReadRequestExecutor.RecordingReadException, Recording]]] =
          instance.source(connSettings, req).runWith(Sink.seq)
        resultFut.map { res =>
          res.map {
            case Left(e) =>
              fail(e)
            case Right(result) =>
              result
          } shouldBe Seq(expected1, expected2)
        }
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Recordings.json"

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlEqualTo(expectedPath)
            )
            .withBasicAuth(connSettings.accountSid.twilioString, "testPassword")
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val req = RecordingReadRequestExecutor.RecordingReadRequest.build(
          _.withAccountSid(connSettings.accountSid).build()
        )

        val resultFut
            : Future[Seq[Either[RecordingReadRequestExecutor.RecordingReadException, Recording]]] =
          instance.source(connSettings, req).runWith(Sink.seq)
        val expected =
          Left(
            RecordingReadRequestExecutor.RecordingReadException.Api(
              ApiException.AuthenticationException()
            )
          )
        resultFut.map { res =>
          res.size shouldBe 1
          res.headOption shouldBe Some(expected)
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {

    val connSettings   = TwilioTestConstants.connSettings(wireMockServer.port())
    val recordingSid   = Recording.Sid.unsafe("REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val callSid1       = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
    val callSid2       = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
    val conferenceSid1 = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
    val conferenceSid2 = Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")

    val encryptionDetails = Recording.EncryptionDetails(
      encryptionType = Recording.EncryptionDetails.EncryptionType.RsaAes,
      publicKeySid =
        Recording.EncryptionDetails.PublicKey.Sid.unsafe("CRXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
      encryptedCek = Recording.EncryptionDetails.ContentEncryptionKey(
        "OV4h6zrsxMIW7h0Zfqwfn6TI2GCNl54KALlg8wn8YB8KYZhXt6HlgvBWAmQTlfYVeLWydMiCewY0YkDDT1xmNe5huEo9vjuKBS5OmYK4CZkSx1NVv3XOGrZHpd2Pl/5WJHVhUK//AUO87uh5qnUP2E0KoLh1nyCLeGcEkXU0RfpPn/6nxjof/n6m6OzZOyeIRK4Oed5+rEtjqFDfqT0EVKjs6JAxv+f0DCc1xYRHl2yV8bahUPVKs+bHYdy4PVszFKa76M/Uae4jFA9Lv233JqWcxj+K2UoghuGhAFbV/JQIIswY2CBYI8JlVSifSqNEl9vvsTJ8bkVMm3MKbG2P7Q=="
      ),
      iv = Recording.EncryptionDetails.InitialVector("8I2hhNIYNTrwxfHk")
    )
    def twilioResponseInvalidCredentials =
      """{
        |  "code": 20003,
        |  "detail": "Your AccountSid or AuthToken was incorrect.",
        |  "message": "Authentication Error - No credentials provided",
        |  "more_info": "https://www.twilio.com/docs/errors/20003",
        |  "status": 401
        |}
        |""".stripMargin

    private val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )
    private val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 38)),
        ZoneOffset.UTC
      )
    )

    private val startAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    def recording(
        accountSid: TwilioAccount.Sid,
        status: Recording.Status,
        callSid: Call.Sid,
        conferenceSid: Conference.Sid
    ) =
      Recording(
        accountSid = accountSid,
        callSid = callSid,
        conferenceSid = Some(conferenceSid),
        dateCreated = createdAtInstant,
        dateUpdate = updatedAtInstant,
        startTime = startAtInstant,
        duration = Some(Duration.ofSeconds(4)),
        sid = recordingSid,
        price = Some(Recording.Price(0.04, Iso4127CountryCode("USD"))),
        status = status,
        channels = Recording.Channels.unsafe(2),
        source = Recording.Source.StartConferenceRecordingAPI,
        errorCode = None,
        encryptionDetails = Some(encryptionDetails),
        mediaUrl = Some(
          Recording.MediaUrl(
            s"http://api.twilio.com/2010-04-01/Accounts/${accountSid.twilioString}/Recordings/${recordingSid.twilioString}"
          )
        ),
        track = None
      )

    def recordingReferenceResp(
        accountSid: TwilioAccount.Sid,
        status: Recording.Status,
        callSid: Call.Sid,
        conferenceSid: Option[Conference.Sid]
    ): String = {
      s"""{
         |      "account_sid": "${accountSid.twilioString}",
         |      "api_version": "2010-04-01",
         |      "call_sid": "${callSid.twilioString}",
         |      "conference_sid": "${conferenceSid.map(c => c.twilioString).getOrElse("null")}",
         |      "channels": 2,
         |      "date_created": "Fri, 14 Oct 2016 21:56:34 +0000",
         |      "date_updated": "Fri, 14 Oct 2016 21:56:38 +0000",
         |      "start_time": "Fri, 14 Oct 2016 21:56:34 +0000",
         |      "price": "0.04",
         |      "price_unit": "USD",
         |      "duration": "4",
         |      "sid": "REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |      "source": "StartConferenceRecordingAPI",
         |      "status": "${status.twilioString}",
         |      "error_code": null,
         |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
         |      "subresource_uris": {
         |        "add_on_results": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/AddOnResults.json",
         |        "transcriptions": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Transcriptions.json"
         |      },
         |      "encryption_details": {
         |        "encryption_public_key_sid": "CRXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |        "encryption_cek": "OV4h6zrsxMIW7h0Zfqwfn6TI2GCNl54KALlg8wn8YB8KYZhXt6HlgvBWAmQTlfYVeLWydMiCewY0YkDDT1xmNe5huEo9vjuKBS5OmYK4CZkSx1NVv3XOGrZHpd2Pl/5WJHVhUK//AUO87uh5qnUP2E0KoLh1nyCLeGcEkXU0RfpPn/6nxjof/n6m6OzZOyeIRK4Oed5+rEtjqFDfqT0EVKjs6JAxv+f0DCc1xYRHl2yV8bahUPVKs+bHYdy4PVszFKa76M/Uae4jFA9Lv233JqWcxj+K2UoghuGhAFbV/JQIIswY2CBYI8JlVSifSqNEl9vvsTJ8bkVMm3MKbG2P7Q==",
         |        "encryption_iv": "8I2hhNIYNTrwxfHk"
         |      },
         |      "media_url": "http://api.twilio.com/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
         |    }""".stripMargin
    }

    def recordingListResp(
        accountSid: TwilioAccount.Sid,
        recordings: List[Recording]
    ): String =
      s"""{
         |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Recordings.json?PageSize=1000&Page=0",
         |    "end": 0,
         |    "recordings": [
         |         ${recordings
          .map(recordings =>
            recordingReferenceResp(
              accountSid,
              recordings.status,
              recordings.callSid,
              recordings.conferenceSid
            )
          )
          .mkString(", ")}
         |    ],
         |    "previous_page_uri": null,
         |    "uri": "/2010-04-01/Accounts/$accountSid/Recordings.json?PageSize=1000&Page=0",
         |    "page_size": 1000,
         |    "start": 0,
         |    "next_page_uri": null,
         |    "page": 0
         |}
         |""".stripMargin

    val instance: RecordingReadRequestExecutor =
      TwilioClient.defaultImpl().voice.recordingRead
  }
}
