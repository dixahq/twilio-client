package com.dixa.twilio.client.twilioClient.messaging

import akka.Done
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor.{
  PhoneNumberDeleteException,
  PhoneNumberDeleteRequest
}
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class MessagingPhoneNumberDeleteTest extends TwilioClientTest {
  classOf[PhoneNumberDeleteRequestExecutor].getSimpleName when {

    "unsafeRun" should {
      "ask twilio to delete it (meaning removing it from a service)" in {
        val f = new Fixture
        import f._

        wiremockStubForSuccess()

        val resultFut: Future[Done] =
          instance.unsafeRun(connSettings, toDelete)
        // Does not return anything, so just make sure to await it, so we fail if it throws an Exception.
        resultFut.map(_ => succeed)
      }
    }

    "run" should {
      "ask twilio to delete it (meaning removing it from a service)" in {
        val f = new Fixture
        import f._

        wiremockStubForSuccess()

        val resultFut: Future[Either[PhoneNumberDeleteException, Done]] =
          instance.run(connSettings, toDelete)
        resultFut.map(result => assert(result === Right(Done)))
      }

      "return undefined error if twilio throws up" in {
        val f = new Fixture
        import f._

        wiremockStubForServerError()

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
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val toDelete = PhoneNumberDeleteRequest(
      serviceSid = TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0"),
      phoneNumberSid = TwilioPhoneNumberSid("PNa2ab2f57a0ffca3a3fa907a4ce305477")
    )

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance     = TwilioClient.defaultImpl().messaging.phoneNumberDelete

    def wiremockStubForSuccess(): Unit = {
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
    }

    def wiremockStubForServerError(): Unit = {
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
    }
  }
}
