package com.dixa.twilio.client.twilioClient.messaging

import akka.Done
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioClientMessaging, TwilioTestConstants}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class MessagingPhoneNumberDeleteTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "asked to delete a Phonenumber" should {
      "ask twilio to delete it (meaning removing it from a service)" in {

        val toDelete = TwilioClientMessaging.PhoneNumberDeleteRequest(
          serviceSid = TwilioMessagingService.Sid("MG777c6a32c5b17bc426e7fff6a0f67aa0"),
          phoneNumberSid = TwilioPhoneNumberSid("PNa2ab2f57a0ffca3a3fa907a4ce305477")
        )

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

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance     = TwilioClient.defaultImpl().messaging
        val resultFut: Future[Done] =
          instance.phoneNumberDelete(connSettings, toDelete)
        // Does not return anything, so just make sure to await it, so we fail if it throws an Exception.
        resultFut.map(_ => succeed)
      }
    }
  }
}
