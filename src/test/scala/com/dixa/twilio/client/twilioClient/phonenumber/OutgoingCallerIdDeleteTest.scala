package com.dixa.twilio.client.twilioClient.phonenumber

import akka.Done
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

final class OutgoingCallerIdDeleteTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientPhoneNumber].getSimpleName when {

    "outgoingCallerIdDelete" should {
      "safely delete a single outgoing caller id from subaccount" in {
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/OutgoingCallerIds/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber
        val req =
          OutgoingCallerIdDeleteRequest(
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            OutgoingCallerId.Sid("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
          )

        val resultFut = instance.outgoingCallerIdDelete.run(twilioConnectionSetting, req)

        resultFut.map(res => assert(res === Right(Done)))
      }
    }
  }
}
