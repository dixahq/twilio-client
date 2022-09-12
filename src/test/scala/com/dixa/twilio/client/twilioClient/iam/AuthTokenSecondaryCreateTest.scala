package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AuthTokenSecondaryCreateRequestExecutor.{
  AuthTokenSecondaryCreateException,
  AuthTokenSecondaryCreateRequest
}
import com.dixa.twilio.client.iam.{AuthTokenSecondaryCreateRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class AuthTokenSecondaryCreateTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to create a new secondary auth token" should {

      "Return the the newly create token" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(201) // this api returns 201 on success.
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected =
          AuthToken.AuthTokenAndMetaData[AuthToken.Secondary](
            AuthToken.Secondary("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
            AuthToken.MetaData(
              TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
              TwilioTestConstants.createdTime,
              TwilioTestConstants.updatedTime
            )
          )

        val resultFut: Future[
          Either[AuthTokenSecondaryCreateException, AuthToken.AuthTokenAndMetaData[
            AuthToken.Secondary
          ]]
        ] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither =>
          val result = resultEither.toTry.get
          assert(result === expected)
        }
      }

      "Return an API error in case of 404" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 20404, "message": "The requested resource /AuthTokens/Secondary was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val expected = Left(AuthTokenSecondaryCreateException.ApiCallNotEnabledOnAccountException())

        val resultFut: Future[
          Either[AuthTokenSecondaryCreateException, AuthToken.AuthTokenAndMetaData[
            AuthToken.Secondary
          ]]
        ] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither => assert(resultEither === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.AccountSid {
    val createRequest = AuthTokenSecondaryCreateRequest()

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v1/AuthTokens/Secondary"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: AuthTokenSecondaryCreateRequestExecutor =
      TwilioClient.defaultImpl().iam.authTokenSecondaryCreate
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "date_created": "2022-08-30T05:14:57Z",
      |  "date_updated": "2022-09-10T18:31:37Z",
      |  "secondary_auth_token": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
      |  "url": "https://accounts.twilio.com/v1/AuthTokens/Secondary"
      |}
      |""".stripMargin
}
