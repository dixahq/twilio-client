package com.dixa.twilio.client.twilioClient.general

import org.apache.pekko.Done
import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.{TwilioClientGeneral, UsageTriggerDeleteRequestExecutor}
import com.dixa.twilio.client.general.UsageTriggerDeleteRequestExecutor.{
  UsageTriggerDeleteException,
  UsageTriggerDeleteRequest
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.general.UsageTrigger
import com.dixa.twilio.model.iam.TwilioAccount
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class UsageTriggerDeleteTest extends TwilioClientTest {

  classOf[TwilioClientGeneral].getSimpleName when {
    "Asked to delete a usage trigger" should {

      "Return Done if it succeeds" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${accountSid.twilioString}/Usage/Triggers/${usageTriggerSid.twilioString}.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204) // this api returns 204 on success.
                .withHeader("Content-Type", "application/json")
            )
        )

        val resultFut: Future[Either[UsageTriggerDeleteException, Done]] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither =>
          val result = resultEither.toTry.get
          assert(result === Done)
        }
      }

      "Return a not existing error in case of 404" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${accountSid.twilioString}/Usage/Triggers/${usageTriggerSid.twilioString}.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  """{"code": 20404, "message": "The requested resource /AuthTokens/Secondary was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val expected =
          Left(
            UsageTriggerDeleteException.UsageTriggerNotFoundOnAccountException(
              createRequest.accountSid,
              createRequest.sid
            )
          )

        val resultFut: Future[Either[UsageTriggerDeleteException, Done]] =
          instance.run(connSettings, createRequest)
        resultFut.map { resultEither => assert(resultEither === expected) }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture extends CommonFixtures.AccountSid {
    val accountSid      = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val usageTriggerSid = UsageTrigger.Sid.unsafe("UTXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
    val createRequest   = UsageTriggerDeleteRequest.builder(
      _.withAccountSid(accountSid)
        .withSid(usageTriggerSid)
        .build()
    )

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance: UsageTriggerDeleteRequestExecutor =
      TwilioClient.defaultImpl().general.usageTriggerDelete
  }
}
