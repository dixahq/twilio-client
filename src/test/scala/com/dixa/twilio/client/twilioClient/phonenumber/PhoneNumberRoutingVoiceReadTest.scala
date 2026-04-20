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

package com.dixa.twilio.client.twilioClient.phonenumber

import com.dixa.twilio.client.phonenumber.PhoneNumberRoutingVoiceReadRequestExecutor._
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, PhoneNumberRoutingVoice}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

final class PhoneNumberRoutingVoiceReadTest extends TwilioClientTest with Matchers {

  private val phoneNumber = PhoneNumberE164.unsafe("+15005550006")
  private val accountSid  = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

  "phoneNumberRoutingVoiceRead" should {

    "return the routing region for a phone number" in {
      wireMockServer.stubFor(
        WireMock
          .get(WireMock.urlPathEqualTo("/v2/PhoneNumbers/+15005550006"))
          .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody(successResponse)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val executor     = TwilioClient.defaultImpl().phoneNumber.phoneNumberRoutingVoiceRead
      val request      = PhoneNumberRoutingVoiceReadRequest.build(
        _.withPhoneNumber(phoneNumber).build()
      )

      executor.run(connSettings, request).map { result =>
        result shouldBe Right(
          PhoneNumberRoutingVoice(
            sid = PhoneNumberRoutingVoice.Sid.unsafe("QQXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            accountSid = accountSid,
            phoneNumber = phoneNumber,
            friendlyName = "my number",
            voiceRegion = "ie1"
          )
        )
      }
    }

    "return PhoneNumberNotFound when Twilio returns 404" in {
      wireMockServer.stubFor(
        WireMock
          .get(WireMock.urlPathEqualTo("/v2/PhoneNumbers/+15005550006"))
          .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
          .willReturn(
            aResponse()
              .withStatus(404)
          )
      )

      val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
      val executor     = TwilioClient.defaultImpl().phoneNumber.phoneNumberRoutingVoiceRead
      val request      = PhoneNumberRoutingVoiceReadRequest.build(
        _.withPhoneNumber(phoneNumber).build()
      )

      executor.run(connSettings, request).map { result =>
        result shouldBe Left(
          PhoneNumberRoutingVoiceReadException.PhoneNumberNotFound(phoneNumber)
        )
      }
    }
  }

  private val successResponse =
    s"""{
       |  "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "date_created": "2026-01-29T14:54:53Z",
       |  "date_updated": "2026-01-29T14:54:53Z",
       |  "friendly_name": "my number",
       |  "phone_number": "+15005550006",
       |  "sid": "QQXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "url": "https://routes.twilio.com/v2/PhoneNumbers/+15005550006",
       |  "voice_region": "ie1"
       |}
       |""".stripMargin
}
