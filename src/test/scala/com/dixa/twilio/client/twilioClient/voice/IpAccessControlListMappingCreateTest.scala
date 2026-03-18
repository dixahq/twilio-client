// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
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
import com.dixa.twilio.client.voice.IpAccessControlListMappingCreateRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.voice.{IpAccessControlList, IpAccessControlListMapping, SipDomain}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class IpAccessControlListMappingCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an IpAccessControlListMapping" should {

      "ask twilio to create it, and return the IpAccessControlListMapping it gets back from Twilio" in {

        val request =
          IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest
            .build(
              _.withAccountSid(CommonFixtures.accountSid1)
                .withDomainSid(SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
                .withIpAccessControlListSid(
                  IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
                )
                .build()
            )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Auth/Calls/IpAccessControlListMappings.json"
              )
            )
            .withRequestBody(
              WireMock.containing("IpAccessControlListSid=ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = IpAccessControlListMapping(
          CommonFixtures.accountSid1,
          SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: IpAccessControlListMappingCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.ipAccessControlListMappingCreate
        val resultFut: Future[
          Either[
            IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateException,
            IpAccessControlListMapping
          ]
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

      "return IpAccessControlListMappingAlreadyExists if Twilio returns 400 with code 21231" in {
        val domainSid = SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        val ipAclSid  = IpAccessControlList.Sid.unsafe("ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

        val request =
          IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateRequest
            .build(
              _.withAccountSid(CommonFixtures.accountSid1)
                .withDomainSid(domainSid)
                .withIpAccessControlListSid(ipAclSid)
                .build()
            )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains/$domainSid/Auth/Calls/IpAccessControlListMappings.json"
              )
            )
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  s"""{"code":21231,"message":"$ipAclSid already associated with $domainSid","more_info":"https://www.twilio.com/docs/errors/21231","status":400}"""
                )
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: IpAccessControlListMappingCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.ipAccessControlListMappingCreate
        val resultFut = instance.run(connSettings, request)

        resultFut.map { result =>
          assert(
            result == Left(
              IpAccessControlListMappingCreateRequestExecutor.IpAccessControlListMappingCreateException
                .IpAccessControlListMappingAlreadyExists(domainSid, ipAclSid)
            )
          )
        }
      }
    }
  }

  private def twilioResponse1 =
    s"""{
       |  "account_sid": "${CommonFixtures.accountSid1}",
       |  "date_created": "Thu, 30 Jul 2015 20:00:00 +0000",
       |  "date_updated": "Thu, 30 Jul 2015 20:00:00 +0000",
       |  "friendly_name": "Does not really matter, as it not the friendly name of the sub resource anyway",
       |  "sid": "ALXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
       |}
       |""".stripMargin
}
