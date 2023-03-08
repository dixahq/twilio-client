package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AuthTokenPromoteRequestExecutor.{
  AuthTokenPromoteException,
  AuthTokenPromoteRequest
}
import com.dixa.twilio.client.iam.{AuthTokenPromoteRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class AuthTokenPromoteTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to promote auth token" should {

      "Return the the newly promoted token" in {
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
          AuthToken.AuthTokenAndMetaData[AuthToken.Primary](
            AuthToken.Primary("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
            AuthToken.MetaData(
              TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
              TwilioTestConstants.createdTime,
              TwilioTestConstants.updatedTime
            )
          )

        val resultFut: Future[
          Either[AuthTokenPromoteException, AuthToken.AuthTokenAndMetaData[
            AuthToken.Primary
          ]]
        ] =
          instance.run(connSettings, promoteRequest)
        resultFut.map { resultEither =>
          val result = resultEither.toTry.get
          assert(result === expected)
        }
      }

      "Return an not existing error in case of 404" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          wireMockBuilderExpectedTwilioRequest
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 20404, "message": "The requested resource /AuthTokens/Promote was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val expected =
          Left(AuthTokenPromoteException.SecondaryAuthTokenNotFoundOnAccountException())

        val resultFut: Future[
          Either[AuthTokenPromoteException, AuthToken.AuthTokenAndMetaData[
            AuthToken.Primary
          ]]
        ] =
          instance.run(connSettings, promoteRequest)
        resultFut.map { resultEither => assert(resultEither === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.AccountSid {
    val promoteRequest = AuthTokenPromoteRequest()

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v1/AuthTokens/Promote"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: AuthTokenPromoteRequestExecutor =
      TwilioClient.defaultImpl().iam.authTokenPromote
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "date_created": "2022-08-30T05:14:57Z",
      |  "date_updated": "2022-09-10T18:31:37Z",
      |  "auth_token": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      |  "url": "https://accounts.twilio.com/v1/AuthTokens/Secondary"
      |}
      |""".stripMargin
}
