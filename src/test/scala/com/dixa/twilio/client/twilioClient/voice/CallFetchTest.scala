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

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.CallFetchRequestExecutor.CallFetchException
import com.dixa.twilio.client.voice.{CallFetchRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{Call, Group, Trunk}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Duration, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class CallFetchTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "asked to fetch a Call" should {

      "return a successful result when queue exists and is fetched" in {
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

        val expected: Either[CallFetchRequestExecutor.CallFetchException, Call] = Right(
          Call(
            sid = callSid,
            dateCreated =
              ZonedDateTime.of(2019, 10, 18, 17, 0, 0, 0, ZoneOffset.ofHours(0)).toInstant,
            dateUpdate =
              ZonedDateTime.of(2019, 10, 18, 17, 1, 0, 0, ZoneOffset.ofHours(0)).toInstant,
            parentCallSid = Call.Sid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX").toOption,
            accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            to = Call.CallerId("+13051913581"),
            toFormatted = Call.FormattedPhoneNumber("(305) 191-3581"),
            from = Call.CallerId("+13051416799"),
            fromFormatted = Call.FormattedPhoneNumber("(305) 141-6799"),
            phoneNumberSid = TwilioPhoneNumber.Sid("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX").toOption,
            status = Call.Status.Completed,
            startTime =
              Some(ZonedDateTime.of(2019, 10, 18, 17, 2, 0, 0, ZoneOffset.ofHours(0)).toInstant),
            endTime =
              Some(ZonedDateTime.of(2019, 10, 18, 17, 3, 0, 0, ZoneOffset.ofHours(0)).toInstant),
            duration = Some(Duration.ofSeconds(4)),
            price = Some(Call.Price(-0.2, Iso4127CountryCode("USD"))),
            direction = Call.Direction.OutboundApi,
            answeredBy = Some(Call.AnsweredBy.Machine),
            forwardedFrom = Some(Call.ForwardedFrom("calledvia")),
            groupSid = Group.Sid("GPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX").toOption,
            callerName = Some(Call.Name("callerid")),
            queueTime = Duration.ofMillis(1000),
            trunkSid = Trunk.Sid("TKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX").toOption
          )
        )

        val resultFut: Future[
          Either[CallFetchRequestExecutor.CallFetchException, Call]
        ] =
          instance.run(connSettings, request)
        resultFut.map(result => assert(result === expected))
      }

      "return a Left if the queue does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseQueueNotFound)
            )
        )

        val resultFut: Future[
          Either[CallFetchRequestExecutor.CallFetchException, Call]
        ] =
          instance.run(connSettings, request)
        val expected = Left(CallFetchException.CallNotFound(connSettings.accountSid, callSid))
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
          Either[CallFetchException, Call]
        ]            = instance.run(connSettings, request)
        val expected =
          Left(CallFetchException.Api(ApiException.AuthenticationException()))
        resultFut.map(res => assert(res === expected))
      }
    }
  }

  val twilioResponse1: String = {
    s"""{
       |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "answered_by": "machine",
       |  "api_version": "2010-04-01",
       |  "caller_name": "callerid",
       |  "date_created": "Fri, 18 Oct 2019 17:00:00 +0000",
       |  "date_updated": "Fri, 18 Oct 2019 17:01:00 +0000",
       |  "direction": "outbound-api",
       |  "duration": "4",
       |  "end_time": "Fri, 18 Oct 2019 17:03:00 +0000",
       |  "forwarded_from": "calledvia",
       |  "from": "+13051416799",
       |  "from_formatted": "(305) 141-6799",
       |  "group_sid": "GPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "parent_call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "phone_number_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "price": "-0.200",
       |  "price_unit": "USD",
       |  "sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "start_time": "Fri, 18 Oct 2019 17:02:00 +0000",
       |  "status": "completed",
       |  "subresource_uris": {
       |    "notifications": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Notifications.json",
       |    "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings.json",
       |    "feedback": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Feedback.json",
       |    "feedback_summaries": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/FeedbackSummary.json",
       |    "payments": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Payments.json",
       |    "events": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Events.json",
       |    "siprec": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Siprec.json",
       |    "streams": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Streams.json",
       |    "user_defined_message_subscriptions": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessageSubscriptions.json",
       |    "user_defined_messages": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessages.json"
       |  },
       |  "to": "+13051913581",
       |  "to_formatted": "(305) 191-3581",
       |  "trunk_sid": "TKXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |  "queue_time": "1000"
       |}""".stripMargin
  }
  private def twilioResponseQueueNotFound =
    """{
      |  "code": 20404,
      |  "message": "The requested resource /2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Queues/QUXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
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
    val request      = CallFetchRequestExecutor.CallFetchRequest.build(
      _.withAccountSid(connSettings.accountSid)
        .withSid(callSid)
        .build()
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .get(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls/$callSid.json"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val instance: CallFetchRequestExecutor =
      TwilioClient.defaultImpl().voice.callFetch
  }
}
