package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipIpAddressReadRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}

final class SipIpAddressReadTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to read SipIpAddresses" should {

      "ask twilio to list them, and return the SipIpAddresses it gets back from Twilio" in {

        val request = SipIpAddressReadRequestExecutor.SipIpAddressReadRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withIpAccessControlListSid(
              IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
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
          SipIpAddress.FriendlyName.unsafe("Unit test ip"),
          SipIpAddress.IpAddress.unsafe("192.168.1.242"),
          Some(SipIpAddress.CidrPrefixLength.`24`),
          IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
          ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipIpAddressReadRequestExecutor =
          TwilioClient.defaultImpl().voice.sipIpAddressRead
        val source = instance.unsafeSource(connSettings, request)
        source.runFold(List.empty[SipIpAddress])(_ :+ _).map { results =>
          assert(results.size === 1)
          assert(results.head === expected)
        }
      }
    }
  }

  private def twilioResponse =
    s"""{
       |  "ip_addresses": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "date_created": "Mon, 20 Jul 2015 17:27:10 +0000",
       |      "date_updated": "Mon, 20 Jul 2015 17:27:10 +0000",
       |      "friendly_name": "Unit test ip",
       |      "ip_access_control_list_sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |      "ip_address": "192.168.1.242",
       |      "cidr_prefix_length": 24,
       |      "sid": "IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |      "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
       |    }
       |  ],
       |  "end": 0,
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses.json?PageSize=50&Page=0",
       |  "next_page_uri": null,
       |  "page": 0,
       |  "page_size": 50,
       |  "previous_page_uri": null,
       |  "start": 0,
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses.json?PageSize=50&Page=0"
       |}
       |""".stripMargin
}
