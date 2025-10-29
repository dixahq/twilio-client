package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{CallCreateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.voice.Call
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time._
import scala.concurrent.Future

final class CallCreateUrlTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {
    "is asked to create a call" should {
      "create a call with a provided url" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse)
            )
        )

        val expected = Right(Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
        val resultFut: Future[
          Either[CallCreateRequestExecutor.CallCreateException, Call.Sid]
        ] = executor.run(connSettings, request)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

  private def twilioResponse =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "answered_by": null,
      |  "api_version": "2010-04-01",
      |  "caller_name": null,
      |  "date_created": null,
      |  "date_updated": null,
      |  "direction": "outbound-api",
      |  "duration": null,
      |  "end_time": null,
      |  "forwarded_from": null,
      |  "from": "+15017122661",
      |  "from_formatted": "(501) 712-2661",
      |  "group_sid": null,
      |  "parent_call_sid": null,
      |  "phone_number_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "price": null,
      |  "price_unit": null,
      |  "sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "start_time": null,
      |  "status": "queued",
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
      |  "to": "+15558675310",
      |  "to_formatted": "(555) 867-5310",
      |  "trunk_sid": null,
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
      |  "queue_time": "1000"
      |}""".stripMargin

  // noinspection TypeAnnotation
  final class Fixture {
    val executor: CallCreateRequestExecutor =
      TwilioClient.defaultImpl().voice.callCreate

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val toCallerId   = Call.CallerId("+15558675310")
    val fromCallerId = Call.CallerId("+15017122661")
    val url          = CallbackUrl.VoiceUrl("http://demo.twilio.com/docs/voice.xml")
    val request      =
      CallCreateRequestExecutor.CallCreateRequest.build(
        _.withAccountSid(connSettings.accountSid)
          .withToCallerId(toCallerId)
          .withFromCallerId(fromCallerId)
          .withUrl(url)
          .build()
      )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Calls.json"
        )
      )
      .withRequestBody(
        WireMock.containing(s"""${URLEncoder.encode("To", "utf-8")}=${URLEncoder
            .encode(toCallerId.twilioString, "utf-8")}""")
      )
      .withRequestBody(
        WireMock.containing(s"""${URLEncoder.encode("From", "utf-8")}=${URLEncoder
            .encode(fromCallerId.twilioString, "utf-8")}""")
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("Url", "utf-8")}=${URLEncoder
              .encode("http://demo.twilio.com/docs/voice.xml", "utf-8")}"""
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

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

  }
}
