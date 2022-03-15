package com.dixa.twilio.client.twilioClient.messaging

import akka.Done
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor.{
  PhoneNumberDeleteException,
  PhoneNumberDeleteRequest
}
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.messaging.ServiceSid
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumberSid
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class MessagingPhoneNumberDeleteTest extends TwilioClientTest {
  classOf[PhoneNumberDeleteRequestExecutor].getSimpleName when {

    "run" should {
      "ask twilio to delete it (meaning removing it from a service), and accept twilio responding with 200" in {
        // Actually twilio do send back a 204 in this case, but let us also support 200, in case they change
        // there mind
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/v1/Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers/PNa2ab2f57a0ffca3a3fa907a4ce305477"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val resultFut: Future[Either[PhoneNumberDeleteException, Done]] =
          instance.run(connSettings, toDelete)
        resultFut.map(result => assert(result === Right(Done)))
      }

      "ask twilio to delete it (meaning removing it from a service), and accept twilio responding with 204" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/v1/Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers/PNa2ab2f57a0ffca3a3fa907a4ce305477"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val resultFut: Future[Either[PhoneNumberDeleteException, Done]] =
          instance.run(connSettings, toDelete)
        resultFut.map(result => assert(result === Right(Done)))
      }

      "return undefined error if twilio throws up" in {
        val f = new Fixture
        import f._

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/v1/Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers/PNa2ab2f57a0ffca3a3fa907a4ce305477"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(500)
                .withBody("AErrorEntityThatShouldBePartOfTheErrorsMsg")
            )
        )

        val resultFut = instance.run(connSettings, toDelete)
        resultFut.map { result =>
          assert(result.isLeft)
          result.left.get match {
            case e: PhoneNumberDeleteException.UnspecifiedError =>
              assert(e.getMessage.contains("AErrorEntityThatShouldBePartOfTheErrorsMsg"))
            case other => fail(s"Wrong error returned: $other")
          }
        }
      }

      "return not found if twilio reports it as not found" in {
        val f = new Fixture
        import f._

        val twilioErrMessage =
          """The requested resource /Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers/PNa2ab2f57a0ffca3a3fa907a4ce305477 was not found"""
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/v1/Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers/PNa2ab2f57a0ffca3a3fa907a4ce305477"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody(s"""{
                             |  "code": 20404,
                             |  "message": "$twilioErrMessage",
                             |  "more_info": "https://www.twilio.com/docs/errors/20404",
                             |  "status": 404
                             |}
                             |""".stripMargin)
            )
        )

        val resultFut = instance.run(connSettings, toDelete)
        resultFut.map { result =>
          assert(result.isLeft)
          result.left.get match {
            case e: PhoneNumberDeleteException.NotFound =>
              assert(e.getMessage === twilioErrMessage)
            case other => fail(s"Wrong error returned: $other")
          }
        }
      }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val toDelete = PhoneNumberDeleteRequest(
      serviceSid = ServiceSid("MG777c6a32c5b17bc426e7fff6a0f67aa0"),
      phoneNumberSid = TwilioPhoneNumberSid("PNa2ab2f57a0ffca3a3fa907a4ce305477")
    )

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance     = TwilioClient.defaultImpl().messaging.phoneNumberDelete
  }
}
