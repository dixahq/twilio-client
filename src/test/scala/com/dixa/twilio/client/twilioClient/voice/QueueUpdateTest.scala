package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.QueueUpdateRequestExecutor.QueueUpdateException
import com.dixa.twilio.client.voice.{QueueUpdateRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.Queue
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{Duration, ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class QueueUpdateTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to update a Queue" should {

      "return a successfull result when updating the max size" in {

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
          Queue(
            queueSid,
            Queue.FriendlyName("TestQueueFriendlyName"),
            connSettings.accountSid,
            Queue.CurrentSize(344),
            Queue.MaxSize(5000),
            Duration.ofSeconds(55),
            ZonedDateTime.of(2015, 8, 4, 18, 39, 9, 0, ZoneOffset.ofHours(0)).toInstant,
            ZonedDateTime.of(2015, 8, 4, 19, 22, 9, 0, ZoneOffset.ofHours(0)).toInstant
          )
        )

        val resultFut: Future[
          Either[QueueUpdateRequestExecutor.QueueUpdateException, Queue]
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
          Either[QueueUpdateRequestExecutor.QueueUpdateException, Queue]
        ] =
          instance.run(connSettings, request)
        val expected = Left(QueueUpdateException.QueueNotFound(connSettings.accountSid, queueSid))
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
          Either[QueueUpdateException, Queue]
        ] = instance.run(connSettings, request)
        val expected =
          Left(QueueUpdateException.Api(ApiException.AuthenticationException()))
        resultFut.map(res => assert(res === expected))
      }
    }
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "average_wait_time": 55,
      |  "current_size": 344,
      |  "date_created": "Tue, 04 Aug 2015 18:39:09 +0000",
      |  "date_updated": "Tue, 04 Aug 2015 19:22:09 +0000",
      |  "friendly_name": "TestQueueFriendlyName",
      |  "max_size": 5000,
      |  "sid": "QUXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Queues/QUXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
      |}
      |""".stripMargin

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
    val queueSid     = Queue.Sid.unsafe("QUXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val request      = QueueUpdateRequestExecutor.QueueUpdateRequest.build(
      _.withAccountSid(connSettings.accountSid)
        .withSid(queueSid)
        .withMaxSize(Queue.MaxSize(5000))
        .build()
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Queues/$queueSid.json"
        )
      )
      .withRequestBody(
        WireMock.containing(
          """MaxSize=5000"""
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val instance: QueueUpdateRequestExecutor =
      TwilioClient.defaultImpl().voice.queueUpdate
  }
}
