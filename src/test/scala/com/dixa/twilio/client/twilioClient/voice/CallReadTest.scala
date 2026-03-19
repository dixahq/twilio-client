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
import com.dixa.twilio.client.voice.CallReadRequestExecutor.CallReadException
import com.dixa.twilio.client.voice.{CallReadRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, TwilioPhoneNumber}
import com.dixa.twilio.model.voice.{Call, Group, Trunk}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

import java.time._
import scala.concurrent.Future

final class CallReadTest extends TwilioClientTest with Matchers {

  classOf[TwilioClientVoice].getSimpleName when {

    "read calls" should {

      "Support reading all calls from account" in {

        val f = new Fixture
        import f._

        val expected1 = call(
          connSettings.accountSid,
          Call.CallerId("+13051416799"),
          Call.CallerId("+13051913581"),
          Call.Status.InProgress,
        )

        val expected2 = call(
          connSettings.accountSid,
          Call.CallerId("+13051416798"),
          Call.CallerId("+13051913580"),
          Call.Status.Completed,
        )

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls.json?" +
          "Status=completed&" +
          "From=%2B13051913581&" +
          "To=%2B13051416799"

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
                  callListResp(
                    accountSid = connSettings.accountSid,
                    calls = List(expected1, expected2)
                  )
                )
            )
        )

        val req = CallReadRequestExecutor.CallReadRequest.build(
          _.withAccountSid(connSettings.accountSid)
            .withTo(PhoneNumberE164.unsafe("+13051416799"))
            .withFrom(PhoneNumberE164.unsafe("+13051913581"))
            .withStatus(Call.Status.Completed)
            .build()
        )

        val resultFut: Future[Seq[Either[CallReadRequestExecutor.CallReadException, Call]]] =
          instance.source(connSettings, req).runWith(Sink.seq)
        resultFut.map { res =>
          res.map {
            case Left(e) =>
              fail(e)
            case Right(result) => result
          } shouldBe Seq(expected1, expected2)
        }
      }

      "Return a Left if credentials are wrong" in {
        val f = new Fixture
        import f._

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls.json?" +
          "Status=completed&" +
          "From=%2B13051913581&" +
          "To=%2B13051416799"

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

        val req = CallReadRequestExecutor.CallReadRequest.build(
          _.withAccountSid(connSettings.accountSid)
            .withTo(PhoneNumberE164.unsafe("+13051416799"))
            .withFrom(PhoneNumberE164.unsafe("+13051913581"))
            .withStatus(Call.Status.Completed)
            .build()
        )

        val resultFut: Future[Seq[Either[CallReadException, Call]]] =
          instance.source(connSettings, req).runWith(Sink.seq)
        val expected =
          Left(CallReadException.Api(ApiException.AuthenticationException()))
        resultFut.map { res =>
          res.size shouldBe 1
          res.headOption shouldBe Some(expected)
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val callSid      = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

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
        LocalDateTime.of(LocalDate.of(2019, 10, 18), LocalTime.of(17, 0, 0)),
        ZoneOffset.UTC
      )
    )
    private val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2019, 10, 18), LocalTime.of(17, 1, 0)),
        ZoneOffset.UTC
      )
    )

    private val startAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2019, 10, 18), LocalTime.of(17, 2, 0)),
        ZoneOffset.UTC
      )
    )

    private val endAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2019, 10, 18), LocalTime.of(17, 3, 0)),
        ZoneOffset.UTC
      )
    )

    def call(
        accountSid: TwilioAccount.Sid,
        to: Call.CallerId,
        from: Call.CallerId,
        status: Call.Status
    ) =
      Call(
        sid = callSid,
        dateCreated = createdAtInstant,
        dateUpdate = updatedAtInstant,
        parentCallSid = Some(callSid),
        accountSid = accountSid,
        to = to,
        toFormatted = Call.FormattedPhoneNumber(to.twilioString),
        from = from,
        fromFormatted = Call.FormattedPhoneNumber(from.twilioString),
        phoneNumberSid = Some(TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")),
        status = status,
        startTime = Some(startAtInstant),
        endTime = Some(endAtInstant),
        duration = Some(Duration.ofSeconds(4)),
        price = Some(Call.Price(-0.200, Iso4127CountryCode("USD"))),
        direction = Call.Direction.OutboundApi,
        answeredBy = Some(Call.AnsweredBy.Machine),
        forwardedFrom = Some(Call.ForwardedFrom("calledvia1")),
        groupSid = Some(Group.Sid.unsafe("GPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")),
        callerName = Some(Call.Name("callerid1")),
        queueTime = Duration.ofSeconds(4),
        trunkSid = Some(Trunk.Sid.unsafe("TKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
      )

    def callReferenceResp(
        accountSid: TwilioAccount.Sid,
        status: Call.Status,
        parentCallSid: Call.Sid,
        toNumber: Call.CallerId,
        fromNumber: Call.CallerId
    ): String = {
      s"""{
         |      "account_sid": "$accountSid",
         |      "annotation": "billingreferencetag1",
         |      "answered_by": "machine",
         |      "api_version": "2010-04-01",
         |      "caller_name": "callerid1",
         |      "date_created": "Fri, 18 Oct 2019 17:00:00 +0000",
         |      "date_updated": "Fri, 18 Oct 2019 17:01:00 +0000",
         |      "direction": "outbound-api",
         |      "duration": "4",
         |      "end_time": "Fri, 18 Oct 2019 17:03:00 +0000",
         |      "forwarded_from": "calledvia1",
         |      "from": "${fromNumber.twilioString}",
         |      "from_formatted": "${fromNumber.twilioString}",
         |      "group_sid": "GPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |      "parent_call_sid": "$parentCallSid",
         |      "phone_number_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |      "price": "-0.2",
         |      "price_unit": "USD",
         |      "sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |      "start_time": "Fri, 18 Oct 2019 17:02:00 +0000",
         |      "status": "${status.twilioString}",
         |      "subresource_uris": {
         |        "feedback": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Feedback.json",
         |        "feedback_summaries": "/2010-04-01/Accounts/$accountSid/Calls/FeedbackSummary.json",
         |        "notifications": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Notifications.json",
         |        "recordings": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings.json",
         |        "payments": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Payments.json",
         |        "events": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Events.json",
         |        "siprec": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Siprec.json",
         |        "streams": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Streams.json",
         |        "user_defined_message_subscriptions": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessageSubscriptions.json",
         |        "user_defined_messages": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessages.json"
         |      },
         |      "to": "${toNumber.twilioString}",
         |      "to_formatted": "${toNumber.twilioString}",
         |      "trunk_sid": "TKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
         |      "uri": "/2010-04-01/Accounts/$accountSid/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
         |      "queue_time": "4000"
         |    }""".stripMargin
    }

    def callListResp(
        accountSid: TwilioAccount.Sid,
        calls: List[Call]
    ): String =
      s"""{
         |    "first_page_uri": "/2010-04-01/Accounts/$accountSid/Calls.json?PageSize=1000&Page=0",
         |    "end": 0,
         |    "calls": [
         |         ${calls
          .map(call =>
            callReferenceResp(
              accountSid,
              call.status,
              call.parentCallSid.get,
              call.to,
              call.from
            )
          )
          .mkString(", ")}
         |    ],
         |    "previous_page_uri": null,
         |    "uri": "/2010-04-01/Accounts/$accountSid/Calls.json?PageSize=1000&Page=0",
         |    "page_size": 1000,
         |    "start": 0,
         |    "next_page_uri": null,
         |    "page": 0
         |}
         |""".stripMargin

    val instance: CallReadRequestExecutor =
      TwilioClient.defaultImpl().voice.callRead
  }
}
