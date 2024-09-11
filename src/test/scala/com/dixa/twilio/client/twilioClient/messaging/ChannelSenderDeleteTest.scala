package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.ApiException.{BadRequestException, NotFound}
import com.dixa.twilio.client.{TwilioClient, TwilioConnectionSettings, TwilioTestConstants}
import com.dixa.twilio.client.messaging.{ChannelSenderDeleteRequestExecutor, ChannelSenderException}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.model.FUnit
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class ChannelSenderDeleteTest extends TwilioClientTest with ChannelSenderTestSharedFixture {

  "TwilioClientMessaging" when {
    "asked to delete a channel sender" should {

      "succeed" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === Right(FUnit)) }
      }

      "fail with bad request exception on bad request status code" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("you asked wrong")
            )
        )

        val expected =
          Left(ChannelSenderException.Api(BadRequestException("you asked wrong")))

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === expected) }
      }

      "fail with not found exception on not found status code" in {

        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(deleteSenderNotFoundError)
            )
        )

        val expected =
          Left(ChannelSenderException.Api(NotFound(deleteSenderNotFoundError)))

        val resultFut = instance.run(connSettings, deleteReq)
        resultFut.map { result => assert(result === expected) }
      }
    }
  }

  final class Fixture {
    val deleteReq: ChannelSenderDeleteRequestExecutor.ChannelSenderDeleteRequest =
      ChannelSenderDeleteRequestExecutor.ChannelSenderDeleteRequest(channelSenderSid =
        channelSenderSid
      )

    val wireMockBuilderExpectedTwilioRequest: MappingBuilder = WireMock
      .delete(
        WireMock.urlPathEqualTo(
          "/v2/Channels/Senders/XEcfd04c72e3397a53e24bd6c7408aff83"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings: TwilioConnectionSettings =
      TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: ChannelSenderDeleteRequestExecutor =
      TwilioClient.defaultImpl().messaging.channelSenderDelete

    val deleteSenderNotFoundError: String = {
      """{
        |"code": 20404,
        |"message": "The requested resource /Channels/Senders/XE58fa6d2ac8c6a868bcc6eab630fd7777 was not found",
        |"more_info": "https://www.twilio.com/docs/errors/20404",
        |"status": 404
        |}""".stripMargin
    }
  }
}
