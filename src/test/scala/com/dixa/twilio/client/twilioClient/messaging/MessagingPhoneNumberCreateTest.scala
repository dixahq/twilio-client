package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.ActiveNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioClientMessaging, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

final class MessagingPhoneNumberCreateTest extends TwilioClientTest {

  private def twilioResponse1 =
    """{
      |  "phone_number": "+4581827622",
      |  "date_updated": "2022-01-27T04:10:55Z",
      |  "capabilities": [
      |    "SMS",
      |    "Voice"
      |  ],
      |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |  "url": "https://messaging.twilio.com/v1/Services/MG771c6a02c8b15bc046e7fff6b0f67aa0/PhoneNumbers/PNa6ab2f33d0ffca5a3fa907a4ce302607",
      |  "country_code": "DK",
      |  "sid": "PNa2ab2f57a0ffca3a3fa907a4ce305477",
      |  "date_created": "2022-01-27T04:10:55Z",
      |  "service_sid": "MG777c6a32c5b17bc426e7fff6a0f67aa0"
      |}
      |""".stripMargin

  private val twilioResponseNumberAlreadyInOtherMesseginService =
    """{
      |  "code": 21712,
      |  "message": "Phone Number or Short Code is associated with another Messaging Service.",
      |  "more_info": "https://www.twilio.com/docs/errors/21712",
      |  "status": 409
      |}
      |""".stripMargin

  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create a Phonenumber" should {
      "ask twilio to create it (meaning adding it to a service), and return the data it " +
        "gets back if unsafe variant is called" in {

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

          val expected = TwilioMessagingPhoneNumber(
            ActiveNumber.Sid("PNa2ab2f57a0ffca3a3fa907a4ce305477"),
            TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0")
          )

          val resultFut: Future[TwilioMessagingPhoneNumber] =
            instance.phoneNumberCreateUnsafe(connSettings, createRequest)
          val result = Await.result(resultFut, 15.seconds)
          assert(result === expected)
        }

      "ask twilio to create it (meaning adding it to a service), and return the data it " +
        "gets back if safe variant is called" in {

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
            TwilioMessagingPhoneNumber(
              ActiveNumber.Sid("PNa2ab2f57a0ffca3a3fa907a4ce305477"),
              TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0")
            )
          )

          val resultFut: Future[
            Either[TwilioClientMessaging.PhoneNumberCreateException, TwilioMessagingPhoneNumber]
          ] =
            instance.phoneNumberCreate(connSettings, createRequest)
          val result = Await.result(resultFut, 15.seconds)
          assert(result === expected)
        }

      "return a failed future if the phone number is already in the specified service, " +
        "and unsafe variant is called" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(
                    """{
                      |  "code": 21710,
                      |  "message": "Phone Number or Short Code is already in the Messaging Service.",
                      |  "more_info": "https://www.twilio.com/docs/errors/21710",
                      |  "status": 409
                      |}""".stripMargin
                  )
              )
          )

          val resultFut: Future[TwilioMessagingPhoneNumber] =
            instance.phoneNumberCreateUnsafe(connSettings, createRequest)
          intercept[
            TwilioClientMessaging.PhoneNumberCreateException.PhoneNumberAlreadyInMessagingService
          ] {
            Await.result(resultFut, 15.seconds)
          }
        }

      "return a failed future if the phone number is already in the specified service, " +
        "and safe variant is called" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(
                    """{
                      |  "code": 21710,
                      |  "message": "Phone Number or Short Code is already in the Messaging Service.",
                      |  "more_info": "https://www.twilio.com/docs/errors/21710",
                      |  "status": 409
                      |}""".stripMargin
                  )
              )
          )

          val resultFut: Future[
            Either[TwilioClientMessaging.PhoneNumberCreateException, TwilioMessagingPhoneNumber]
          ] =
            instance.phoneNumberCreate(connSettings, createRequest)

          val result = Await.result(resultFut, 15.seconds)
          assert(result.isLeft)
          assert(
            result.left.get.isInstanceOf[
              TwilioClientMessaging.PhoneNumberCreateException.PhoneNumberAlreadyInMessagingService
            ]
          )

        }

      "return a failed future if the phone number is already in another service, " +
        "and unsafe variant is called" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(twilioResponseNumberAlreadyInOtherMesseginService)
              )
          )

          val resultFut: Future[TwilioMessagingPhoneNumber] =
            instance.phoneNumberCreateUnsafe(connSettings, createRequest)
          intercept[
            TwilioClientMessaging.PhoneNumberCreateException.PhoneNumberAssociatedWithOtherMessagingService
          ] {
            Await.result(resultFut, 15.seconds)
          }
        }

      "return a failed future if the phone number is already in another service, " +
        "and safe variant is called" in {
          val f = new Fixture
          import f._

          wireMockServer.stubFor(
            wireMockBuilderExpectedTwilioRequest
              .willReturn(
                aResponse()
                  .withStatus(409)
                  .withHeader("Content-Type", "application/json")
                  .withBody(twilioResponseNumberAlreadyInOtherMesseginService)
              )
          )

          val resultFut: Future[
            Either[TwilioClientMessaging.PhoneNumberCreateException, TwilioMessagingPhoneNumber]
          ] =
            instance.phoneNumberCreate(connSettings, createRequest)

          val result = Await.result(resultFut, 15.seconds)
          assert(result.isLeft)
          assert(
            result.left.get.isInstanceOf[
              TwilioClientMessaging.PhoneNumberCreateException.PhoneNumberAssociatedWithOtherMessagingService
            ]
          )
        }
    }
  }

  // noinspection TypeAnnotation
  final class Fixture {
    val createRequest = TwilioClientMessaging.PhoneNumberCreateRequest(
      serviceSid = TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0"),
      activeNumberSid = ActiveNumber.Sid("PNa2ab2f57a0ffca3a3fa907a4ce305477")
    )

    val wireMockBuilderExpectedTwilioRequest = WireMock
      .post(
        WireMock.urlPathEqualTo(
          "/v1/Services/MG777c6a32c5b17bc426e7fff6a0f67aa0/PhoneNumbers"
        )
      )
      .withRequestBody(
        WireMock.containing("PhoneNumberSid=PNa2ab2f57a0ffca3a3fa907a4ce305477")
      )
      .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
      .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))

    val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
    val instance     = TwilioClient.defaultImpl().messaging
  }
}
