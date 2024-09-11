package com.dixa.twilio.client.twilioClient.general

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.ApplicationDeleteRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.general.Application
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ApplicationDeleteTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to delete an Application" should {
      "ask twilio to delete it" in {

        val request = ApplicationDeleteRequestExecutor.ApplicationDeleteRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withSid(Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX536"))
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX536.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ApplicationDeleteRequestExecutor =
          TwilioClient.defaultImpl().general.applicationDelete
        val resultFut: Future[
          Either[ApplicationDeleteRequestExecutor.ApplicationDeleteException, FUnit]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === FUnit)
        }
      }
    }
  }
}
