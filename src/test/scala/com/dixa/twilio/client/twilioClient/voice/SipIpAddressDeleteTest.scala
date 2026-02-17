package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipIpAddressDeleteRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class SipIpAddressDeleteTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to delete a SipIpAddress" should {

      "ask twilio to delete it, and return success" in {

        val request = SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withIpAccessControlListSid(
              IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .withSid(SipIpAddress.Sid.unsafe("IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipIpAddressDeleteRequestExecutor =
          TwilioClient.defaultImpl().voice.sipIpAddressDelete
        val resultFut: Future[
          Either[SipIpAddressDeleteRequestExecutor.SipIpAddressDeleteException, FUnit]
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
