// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.twilioClient.voice

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipIpAddressCreateRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class SipIpAddressCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an SipIpAddress" should {

      "ask twilio to create it, and return the SipIpAddress it gets back from Twilio" in {

        val request = SipIpAddressCreateRequestExecutor.SipIpAddressCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withIpAccessControlListSid(
              IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .withFriendlyName(SipIpAddress.FriendlyName.unsafe("Unit test ip"))
            .withIpAddress(SipIpAddress.IpAddress.unsafe("192.168.1.242"))
            .withCidrPrefixLength(SipIpAddress.CidrPrefixLength.`24`)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/IpAccessControlLists/ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAddresses.json"
              )
            )
            .withRequestBody(WireMock.containing(s"FriendlyName=Unit+test+ip"))
            .withRequestBody(WireMock.containing("IpAddress=192.168.1.242"))
            .withRequestBody(WireMock.containing("CidrPrefixLength=24"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
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
        val instance: SipIpAddressCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.sipIpAddressCreate
        val resultFut: Future[
          Either[SipIpAddressCreateRequestExecutor.SipIpAddressCreateException, SipIpAddress]
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

  private def twilioResponse1 =
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
