package com.dixa.twilio.client.twilioClient.iam

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.{AccountFetchException, AccountFetchRequest}
import com.dixa.twilio.client.iam.{AccountFetchRequestExecutor, TwilioClientIam}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.{AuthToken, TwilioAccount}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class SecondaryAuthTokenCreateTest extends TwilioClientTest {

  classOf[TwilioClientIam].getSimpleName when {
    "Asked to create a new secondary auth token" should {

      "Return the the newly create token" in {
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

        val expected = Right(SecondaryAuthTokenCreateResponse(
          TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

        ))

        val resultFut: Future[
          Either[SecondaryTokenCreateException, ]
        ] =
          instance.run(connSettings, fetchRequest)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.AccountSid {
    val fetchRequest = SecondaryAuthTokenCreateRequest()

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v1/AuthTokens/Secondary"
        )
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: SecondaryAuthTokenCreateRequestExecutor =
      TwilioClient.defaultImpl().iam.secondaryAuthTokenCreate
  }

  private def twilioResponse1 =
    """{
      |  "account_sid": "ACXXXXXXXXXACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "date_created": "2015-07-31T04:00:00Z",
      |  "date_updated": "2015-07-31T04:00:00Z",
      |  "secondary_auth_token": "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
      |  "url": "https://accounts.twilio.com/v1/AuthTokens/Secondary"
      |}
      |""".stripMargin
}
