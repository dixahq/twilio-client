package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{CallCreateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.voice.Call
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URLEncoder
import java.time._
import scala.concurrent.Future

final class CallCreateStatusCallbackEventTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {
    "is asked to create a call" should {
      "create a call with provided status callback events" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
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
      |  "date_created": "Tue, 31 Aug 2010 20:36:28 +0000",
      |  "date_updated": "Tue, 31 Aug 2010 20:36:44 +0000",
      |  "direction": "inbound",
      |  "duration": "15",
      |  "end_time": "Tue, 31 Aug 2010 20:36:44 +0000",
      |  "forwarded_from": "+141586753093",
      |  "from": "+18668675310",
      |  "from_formatted": "(866) 867-5310",
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
      |    "streams": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Streams.json",
      |    "user_defined_message_subscriptions": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessageSubscriptions.json",
      |    "user_defined_messages": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/UserDefinedMessages.json"
      |  },
      |  "to": "+14155551212",
      |  "to_formatted": "(415) 555-1212",
      |  "trunk_sid": null,
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Calls/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
      |  "queue_time": "1000"
      |}""".stripMargin

  // noinspection TypeAnnotation
  final class Fixture {
    val executor: CallCreateRequestExecutor =
      TwilioClient.defaultImpl().voice.callCreate

    val connSettings      = TwilioTestConstants.connSettings(wireMockServer.port())
    val toCallerId        = Call.CallerId("+14155551212")
    val fromCallerId      = Call.CallerId("+18668675310")
    val url               = CallbackUrl("http://demo.twilio.com/docs/voice.xml")
    val statusCallbackUrl = CallbackUrl.VoiceStatusCallbackUrl("https://www.myapp.com/events")
    val request =
      CallCreateRequestExecutor.CallCreateRequest.build(
        _.withAccountSid(connSettings.accountSid)
          .withToCallerId(toCallerId)
          .withFromCallerId(fromCallerId)
          .withUrl(url)
          .withMethod(HttpMethod.Get)
          .withStatusCallback(statusCallbackUrl)
          .withStatusCallbackMethod(HttpMethod.Post)
          .withStatusCallbackEvents(Seq(Call.ProgressEvent.Initiated, Call.ProgressEvent.Answered))
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
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("Method", "utf-8")}=${URLEncoder.encode("GET", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("StatusCallback", "utf-8")}=${URLEncoder
              .encode("https://www.myapp.com/events", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("StatusCallbackMethod", "utf-8")}=${URLEncoder
              .encode("POST", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("StatusCallbackEvent", "utf-8")}=${URLEncoder
              .encode("initiated", "utf-8")}"""
        )
      )
      .withRequestBody(
        WireMock.containing(
          s"""${URLEncoder.encode("StatusCallbackEvent", "utf-8")}=${URLEncoder
              .encode("answered", "utf-8")}"""
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
