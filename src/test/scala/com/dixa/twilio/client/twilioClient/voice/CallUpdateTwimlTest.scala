package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.CallUpdateRequestExecutor.CallUpdateException
import com.dixa.twilio.client.voice.{CallUpdateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, TwilioPhoneNumber}
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.Call
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, OffsetDateTime, ZoneOffset}
import scala.concurrent.Future

final class CallUpdateTwimlTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to update a call" should {

      "Support sending new TwiML to the call" in {

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

        val expected = Right(
          Call(
            sid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            accountSid = connSettings.accountSid,
            answeredBy = None,
            callerName = None,
            dateCreated = createdAtInstant,
            dateUpdate = updatedAtInstant,
            direction = Call.Direction.Inbound,
            duration = Some(Call.Duration("15")),
            endTime = Some(endTimeAtInstant),
            forwardedFrom = Some(Call.ForwardedFrom("+141586753093")),
            from = PhoneNumberE164("+14158675308"),
            fromFormatted = Call.FormattedPhoneNumber("(415) 867-5308"),
            groupSid = None,
            parentCallSid = None,
            phoneNumberSid = TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            price = Some(Call.Price(BigDecimal("-0.0300"), Iso4127CountryCode("USD"))),
            startTime = Some(startTimeAtInstant),
            status = Call.Status.Completed,
            to = PhoneNumberE164("+14158675309"),
            toFormatted = Call.FormattedPhoneNumber("(415) 867-5309"),
            trunkSid = None,
            queueTime = Call.QueueTime("1000"),
          )
        )

        val resultFut: Future[
          Either[CallUpdateRequestExecutor.CallUpdateException, Call]
        ] =
          instance.run(connSettings, request)
        resultFut.map { result =>
          result.left.map { ex =>
            ex.getStackTrace.map(println)
            println(ex)

          }

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
          Either[CallUpdateRequestExecutor.CallUpdateException, Call]
        ] =
          instance.run(connSettings, request)
        val expected = Left(CallUpdateException.CallNotFound(connSettings.accountSid, callSid))
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
          Either[CallUpdateException, Call]
        ] = instance.run(connSettings, request)
        val expected =
          Left(CallUpdateException.Api(ApiException.AuthenticationException()))
        resultFut.map(res => assert(res === expected))
      }
    }
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "answered_by": null,
      |  "api_version": "2010-04-01",
      |  "caller_name": null,
      |  "date_created": "Tue, 31 Aug 2010 20:36:28 +0000",
      |  "date_updated": "Tue, 31 Aug 2010 20:36:44 +0000",
      |  "direction": "inbound",
      |  "duration": "15",
      |  "end_time": "Tue, 31 Aug 2010 20:36:44 +0000",
      |  "forwarded_from": "+141586753093",
      |  "from": "+14158675308",
      |  "from_formatted": "(415) 867-5308",
      |  "group_sid": null,
      |  "parent_call_sid": null,
      |  "phone_number_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "price": "-0.03000",
      |  "price_unit": "USD",
      |  "sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "start_time": "Tue, 31 Aug 2010 20:36:29 +0000",
      |  "status": "completed",
      |  "subresource_uris": {
      |    "notifications": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Notifications.json",
      |    "recordings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Recordings.json",
      |    "feedback": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Feedback.json",
      |    "feedback_summaries": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/FeedbackSummary.json",
      |    "payments": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Payments.json",
      |    "events": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Events.json",
      |    "siprec": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Siprec.json",
      |    "streams": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Streams.json"
      |  },
      |  "to": "+14158675309",
      |  "to_formatted": "(415) 867-5309",
      |  "trunk_sid": null,
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
      |  "queue_time": "1000"
      |}
      |""".stripMargin

  private def twilioResponseCallNotFound =
    """{
      |  "code": 20404, 
      |  "message": "The requested resource /2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
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
    val request = CallUpdateRequestExecutor.CallUpdateRequest.build(
      _.withAccountSid(connSettings.accountSid)
        .withCallSid(callSid)
        .withTwiml(Response.build { _.addSay(_.withText("Ahoy there").build()).buildVerified() })
        .build
    )

    val createdAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2010, 8, 31), LocalTime.of(20, 36, 28)),
        ZoneOffset.UTC
      )
    )

    val updatedAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2010, 8, 31), LocalTime.of(20, 36, 44)),
        ZoneOffset.UTC
      )
    )

    val endTimeAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2010, 8, 31), LocalTime.of(20, 36, 44)),
        ZoneOffset.UTC
      )
    )

    val startTimeAtInstant = Instant.from(
      OffsetDateTime.of(
        LocalDateTime.of(LocalDate.of(2010, 8, 31), LocalTime.of(20, 36, 29)),
        ZoneOffset.UTC
      )
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls/$callSid.json"
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("Twiml", "utf-8")}=${URLEncoder.encode(
              """<?xml version="1.0" encoding="UTF-8"?><Response><Say>Ahoy there</Say></Response>""",
              "utf-8"
            )}"""
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val instance: CallUpdateRequestExecutor =
      TwilioClient.defaultImpl().voice.callUpdate
  }
}
