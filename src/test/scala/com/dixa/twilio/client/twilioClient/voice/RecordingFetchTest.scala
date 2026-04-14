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
import com.dixa.twilio.client.voice.{RecordingFetchRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.voice.Recording.Price
import com.dixa.twilio.model.voice.{Call, Conference, Recording}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time._
import scala.concurrent.Future

final class RecordingFetchTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to fetch call recordings" should {

      "Support fetching a specific call recording" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
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
            conferenceSid = Some(Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")),
            channels = Recording.Channels.unsafe(1),
            dateCreated = createdAtInstant,
            dateUpdate = updatedAtInstant,
            startTime = startTimeAtInstant,
            price = Some(Price(-0.0025, Iso4127CountryCode("USD"))),
            duration = Some(Duration.ofSeconds(4)),
            sid = recordingSid,
            source = Recording.Source.StartConferenceRecordingAPI,
            status = Recording.Status.Completed,
            encryptionDetails = Some(
              Recording.EncryptionDetails(
                Recording.EncryptionDetails.EncryptionType.RsaAes,
                Recording.EncryptionDetails.PublicKey.Sid
                  .unsafe("CRXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
                Recording.EncryptionDetails.ContentEncryptionKey(
                  "OV4h6zrsxMIW7h0Zfqwfn6TI2GCNl54KALlg8wn8YB8KYZhXt6HlgvBWAmQTlfYVeLWydMiCewY0YkDDT1xmNe5huEo9vjuKBS5OmYK4CZkSx1NVv3XOGrZHpd2Pl/5WJHVhUK//AUO87uh5qnUP2E0KoLh1nyCLeGcEkXU0RfpPn/6nxjof/n6m6OzZOyeIRK4Oed5+rEtjqFDfqT0EVKjs6JAxv+f0DCc1xYRHl2yV8bahUPVKs+bHYdy4PVszFKa76M/Uae4jFA9Lv233JqWcxj+K2UoghuGhAFbV/JQIIswY2CBYI8JlVSifSqNEl9vvsTJ8bkVMm3MKbG2P7Q=="
                ),
                Recording.EncryptionDetails.InitialVector("8I2hhNIYNTrwxfHk")
              )
            ),
            mediaUrl = Some(
              Recording.MediaUrl(
                "http://api.twilio.com/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
              )
            )
          )

        val resultFut: Future[
          Either[
            RecordingFetchRequestExecutor.RecordingFetchException,
            Recording
          ]
        ] = instance.run(connSettings, req)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(result) =>
            assert(result === expected)
        }
      }

      "return a Left if the call does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseCallNotFound)
            )
        )

        val resultFut: Future[
          Either[
            RecordingFetchRequestExecutor.RecordingFetchException,
            Recording
          ]
        ] =
          instance.run(connSettings, req)
        val expected = Left(
          RecordingFetchRequestExecutor.RecordingFetchException
            .RecordingNotFound(TwilioTestConstants.accountSid, recordingSid)
        )
        resultFut.map(res => assert(res === expected))
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseInvalidCredentials)
            )
        )

        val resultFut: Future[
          Either[
            RecordingFetchRequestExecutor.RecordingFetchException,
            Recording
          ]
        ]            = instance.run(connSettings, req)
        val expected =
          Left(
            RecordingFetchRequestExecutor.RecordingFetchException.Api(
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
      |  "date_updated": "Fri, 14 Oct 2016 21:56:38 +0000",
      |  "start_time": "Fri, 14 Oct 2016 21:56:34 +0000",
      |  "price": "-0.00250",
      |  "price_unit": "USD",
      |  "duration": "4",
      |  "sid": "REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "source": "StartConferenceRecordingAPI",
      |  "status": "completed",
      |  "error_code": null,
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
      |  "subresource_uris": {
      |    "add_on_results": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/AddOnResults.json",
      |    "transcriptions": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Transcriptions.json"
      |  },
      |  "encryption_details": {
      |    "encryption_public_key_sid": "CRXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |    "encryption_cek": "OV4h6zrsxMIW7h0Zfqwfn6TI2GCNl54KALlg8wn8YB8KYZhXt6HlgvBWAmQTlfYVeLWydMiCewY0YkDDT1xmNe5huEo9vjuKBS5OmYK4CZkSx1NVv3XOGrZHpd2Pl/5WJHVhUK//AUO87uh5qnUP2E0KoLh1nyCLeGcEkXU0RfpPn/6nxjof/n6m6OzZOyeIRK4Oed5+rEtjqFDfqT0EVKjs6JAxv+f0DCc1xYRHl2yV8bahUPVKs+bHYdy4PVszFKa76M/Uae4jFA9Lv233JqWcxj+K2UoghuGhAFbV/JQIIswY2CBYI8JlVSifSqNEl9vvsTJ8bkVMm3MKbG2P7Q==",
      |    "encryption_iv": "8I2hhNIYNTrwxfHk"
      |  },
      |  "media_url": "http://api.twilio.com/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
      |}
      |""".stripMargin

  private def twilioResponseCallNotFound =
    """{
      |  "code": 20404,
      |  "message": "The requested resource /2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
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

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val callSid      = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val recordingSid = Recording.Sid.unsafe("REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val req          =
      RecordingFetchRequestExecutor.RecordingFetchRequest.build(
        _.withAccountSid(TwilioTestConstants.accountSid)
          .withSid(recordingSid)
          .withIncludeSoftDeleted(true)
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
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 38)),
        ZoneOffset.UTC
      )
    )

    val startTimeAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2016, 10, 14), LocalTime.of(21, 56, 34)),
        ZoneOffset.UTC
      )
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${TwilioTestConstants.accountSid}/Recordings/$recordingSid.json"
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("IncludeSoftDeleted", "utf-8")}=${URLEncoder
              .encode("true", "utf-8")}"""
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val instance: RecordingFetchRequestExecutor =
      TwilioClient.defaultImpl().voice.recordingFetch
  }
}
