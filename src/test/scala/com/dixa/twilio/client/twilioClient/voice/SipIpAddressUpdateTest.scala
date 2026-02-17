package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipIpAddressUpdateRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class SipIpAddressUpdateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to update a SipIpAddress" should {

      "ask twilio to update it, and return the SipIpAddress it gets back from Twilio" in {

        val request = SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withIpAccessControlListSid(
              IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .withSid(SipIpAddress.Sid.unsafe("IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
            .withFriendlyName(SipIpAddress.FriendlyName.unsafe("Updated ip name"))
            .withIpAddress(SipIpAddress.IpAddress.unsafe("10.0.0.1"))
            .withCidrPrefixLength(SipIpAddress.CidrPrefixLength.`16`)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
              )
            )
            .withRequestBody(WireMock.containing("FriendlyName=Updated+ip+name"))
            .withRequestBody(WireMock.containing("IpAddress=10.0.0.1"))
            .withRequestBody(WireMock.containing("CidrPrefixLength=16"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse)
            )
        )

        val expected = SipIpAddress(
          SipIpAddress.Sid.unsafe("IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          CommonFixtures.accountSid1,
          SipIpAddress.FriendlyName.unsafe("Updated ip name"),
          SipIpAddress.IpAddress.unsafe("10.0.0.1"),
          Some(SipIpAddress.CidrPrefixLength.`16`),
          IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
          ZonedDateTime.of(2015, 7, 20, 18, 30, 0, 0, ZoneOffset.UTC).toInstant
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipIpAddressUpdateRequestExecutor =
          TwilioClient.defaultImpl().voice.sipIpAddressUpdate
        val resultFut: Future[
          Either[SipIpAddressUpdateRequestExecutor.SipIpAddressUpdateException, SipIpAddress]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === expected)
        }
      }
    }
  }

  private def twilioResponse =
    s"""{
       |  "account_sid": "${CommonFixtures.accountSid1}",
       |  "date_created": "Mon, 20 Jul 2015 17:27:10 +0000",
       |  "date_updated": "Mon, 20 Jul 2015 18:30:00 +0000",
       |  "friendly_name": "Updated ip name",
       |  "ip_access_control_list_sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "ip_address": "10.0.0.1",
       |  "cidr_prefix_length": 16,
       |  "sid": "IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
       |}
       |""".stripMargin
}
