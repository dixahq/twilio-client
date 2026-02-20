package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipIpAddressFetchRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class SipIpAddressFetchTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to fetch a SipIpAddress" should {

      "ask twilio to fetch it, and return the SipIpAddress it gets back from Twilio" in {

        val request = SipIpAddressFetchRequestExecutor.SipIpAddressFetchRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withIpAccessControlListSid(
              IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .withSid(SipIpAddress.Sid.unsafe("IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
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
        val instance: SipIpAddressFetchRequestExecutor =
          TwilioClient.defaultImpl().voice.sipIpAddressFetch
        val resultFut: Future[
          Either[SipIpAddressFetchRequestExecutor.SipIpAddressFetchException, SipIpAddress]
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
       |  "date_updated": "Mon, 20 Jul 2015 17:27:10 +0000",
       |  "friendly_name": "Unit test ip",
       |  "ip_access_control_list_sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "ip_address": "192.168.1.242",
       |  "cidr_prefix_length": 24,
       |  "sid": "IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses/IPXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
       |}
       |""".stripMargin
}
