package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.{RecordingDeleteRequestExecutor, TwilioClientVoice}
import com.dixa.twilio.client.{ApiException, TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.Recording
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class RecordingDeleteTest extends TwilioClientTest {

  classOf[TwilioClientVoice].getSimpleName when {

    "ask to delete a recording" should {

      "Support deleting a recording" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(204)
                .withHeader("Content-Type", "application/json")
            )
        )

        val resultFut: Future[
          Either[
            RecordingDeleteRequestExecutor.RecordingDeleteRequestException,
            Unit
          ]
        ] = instance.run(connSettings, req)
        resultFut.map {
          case Left(e) =>
            fail(e)
          case Right(_) =>
            succeed
        }
      }

      "return a Left if the recording does not exists" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseRecordingNotFound)
            )
        )

        val resultFut: Future[
          Either[
            RecordingDeleteRequestExecutor.RecordingDeleteRequestException,
            Unit
          ]
        ] =
          instance.run(connSettings, req)
        val expected = Left(
          RecordingDeleteRequestExecutor.RecordingDeleteRequestException
            .RecordingNotFound(connSettings.accountSid, recordingSid)
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
            RecordingDeleteRequestExecutor.RecordingDeleteRequestException,
            Unit
          ]
        ] = instance.run(connSettings, req)
        val expected =
          Left(
            RecordingDeleteRequestExecutor.RecordingDeleteRequestException.Api(
              ApiException.AuthenticationException()
            )
          )
        resultFut.map(res => assert(res === expected))
      }
    }
  }

  private def twilioResponseRecordingNotFound =
    """{
      |  "code": 20404,
      |  "message": "The requested resource /2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/Recordings/REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json was not found",
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
    val recordingSid = Recording.Sid.unsafe("REXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val req          =
      RecordingDeleteRequestExecutor.RecordingDeleteRequest.build(
        _.withAccountSid(connSettings.accountSid)
          .withSid(recordingSid)
          .build()
      )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .delete(
        WireMock.urlPathEqualTo(
          s"/2010-04-01/Accounts/${connSettings.accountSid}/Recordings/$recordingSid.json"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val instance: RecordingDeleteRequestExecutor =
      TwilioClient.defaultImpl().voice.recordingDelete
  }
}
