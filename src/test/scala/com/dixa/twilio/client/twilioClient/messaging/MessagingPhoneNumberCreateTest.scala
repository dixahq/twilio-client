package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.TwilioActiveNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioClientMessaging, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

final class MessagingPhoneNumberCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask complete create a Phonenumber" should {
      "ask twilio to create it (meaning adding it to a service), and return the data it gets back" in {

        val toCreate = TwilioClientMessaging.PhoneNumberCreateRequest(
          serviceSid = TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0"),
          activeNumberSid = TwilioActiveNumber.Sid("PNa2ab2f57a0ffca3a3fa907a4ce305477")
        )

        wireMockServer.stubFor(
          WireMock
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
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = TwilioMessagingPhoneNumber(
          TwilioActiveNumber.Sid("PNa2ab2f57a0ffca3a3fa907a4ce305477"),
          TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0")
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance     = TwilioClient.defaultImpl().messaging
        val resultFut: Future[TwilioMessagingPhoneNumber] =
          instance.phoneNumberCreate(connSettings, toCreate)
        resultFut.map(result => assert(result === expected))
      }
    }
  }

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
}
