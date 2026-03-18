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
import com.dixa.twilio.client.voice.SipDomainCreateRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{ByocTrunk, SipDomain}
import com.dixa.twilio.model.{CallbackUrlOptionalAndRequiredMethod, HttpMethod}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class SipDomainCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an SipDomain" should {

      "return InvalidDomainName when Twilio responds with error code 21232" in {
        val domainName = SipDomain.DomainName.unsafe("bad-subdomain.nonexistent.sip.twilio.com")
        val request    = SipDomainCreateRequestExecutor.SipDomainCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withDomainName(domainName)
            .withVoiceUrl(CallbackUrl.VoiceUrl("http://unit.test/voice/url"))
            .withVoiceFallbackUrl(CallbackUrl.VoiceFallbackUrl("http://unit.test/voice/fallback"))
            .withVoiceStatusCallbackUrl(
              CallbackUrl.VoiceStatusCallbackUrl("http://unit.test/voice/status")
            )
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json"
              )
            )
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioError21232)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipDomainCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.sipDomainCreate
        instance.run(connSettings, request).map { result =>
          assert(
            result === Left(
              SipDomainCreateRequestExecutor.SipDomainCreateException.InvalidDomainName(domainName)
            )
          )
        }
      }

      "return InvalidDomainName when Twilio responds with error code 21231" in {
        val domainName = SipDomain.DomainName.unsafe("ie1.ankr-test..sip.twilio.com")
        val request    = SipDomainCreateRequestExecutor.SipDomainCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withDomainName(domainName)
            .withVoiceUrl(CallbackUrl.VoiceUrl("http://unit.test/voice/url"))
            .withVoiceFallbackUrl(CallbackUrl.VoiceFallbackUrl("http://unit.test/voice/fallback"))
            .withVoiceStatusCallbackUrl(
              CallbackUrl.VoiceStatusCallbackUrl("http://unit.test/voice/status")
            )
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json"
              )
            )
            .willReturn(
              aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioError21231)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipDomainCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.sipDomainCreate
        instance.run(connSettings, request).map { result =>
          assert(
            result === Left(
              SipDomainCreateRequestExecutor.SipDomainCreateException.InvalidDomainName(domainName)
            )
          )
        }
      }

      "ask twilio to create it, and return the SipDomain it gets back from Twilio" in {

        val request = SipDomainCreateRequestExecutor.SipDomainCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withDomainName(SipDomain.DomainName.unsafe("unitTest.sip.twilio.com"))
            .withFriendlyName(SipDomain.FriendlyName.unsafe("Unit test domain name"))
            .withVoiceUrl(CallbackUrl.VoiceUrl("http://unit.test/voice/url"))
            .withVoiceMethod(HttpMethod.Post)
            .withVoiceFallbackUrl(CallbackUrl.VoiceFallbackUrl("http://unit.test/voice/fallback"))
            .withVoiceFallbackMethod(HttpMethod.Post)
            .withVoiceStatusCallbackUrl(
              CallbackUrl.VoiceStatusCallbackUrl("http://unit.test/voice/status")
            )
            .withVoiceStatusCallbackMethod(HttpMethod.Post)
            .withSipRegistration(true)
            .withEmergencyCallingEnabled(true)
            .withSecure(true)
            // We don't have the specific sid types yet, so just use a a call sid for now.
            .withByocTrunkSid(ByocTrunk.Sid.unsafe("BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"))
            .withEmergencyCallerSid(
              TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
            )
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json"
              )
            )
            .withRequestBody(WireMock.containing(s"DomainName=unitTest.sip.twilio.com"))
            .withRequestBody(WireMock.containing(s"FriendlyName=Unit+test+domain+name"))
            .withRequestBody(
              WireMock.containing("VoiceUrl=http%3A%2F%2Funit.test%2Fvoice%2Furl")
            )
            .withRequestBody(WireMock.containing("VoiceMethod=POST"))
            .withRequestBody(
              WireMock.containing("VoiceFallbackUrl=http%3A%2F%2Funit.test%2Fvoice%2Ffallback")
            )
            .withRequestBody(WireMock.containing(s"VoiceFallbackMethod=POST"))
            .withRequestBody(
              WireMock.containing(s"VoiceStatusCallbackUrl=http%3A%2F%2Funit.test%2Fvoice%2Fstatus")
            )
            .withRequestBody(WireMock.containing(s"VoiceStatusCallbackMethod=POST"))
            .withRequestBody(WireMock.containing(s"SipRegistration=true"))
            .withRequestBody(WireMock.containing(s"EmergencyCallingEnabled=true"))
            .withRequestBody(WireMock.containing(s"Secure=true"))
            .withRequestBody(
              WireMock.containing(s"ByocTrunkSid=BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
            )
            .withRequestBody(
              WireMock.containing(s"EmergencyCallerSid=PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2")
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
        val expected = SipDomain(
          CommonFixtures.accountSid1,
          Some(SipDomain.AuthType.IpAcl),
          ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
          ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
          SipDomain.DomainName.unsafe("unitTest.sip.twilio.com"),
          Some(SipDomain.FriendlyName.unsafe("Unit test domain name")),
          SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          CallbackUrlOptionalAndRequiredMethod(
            Some(CallbackUrl.VoiceFallbackUrl("http://unit.test/voice/fallback")),
            HttpMethod.Post
          ),
          CallbackUrlOptionalAndRequiredMethod(
            Some(CallbackUrl.VoiceStatusCallbackUrl("http://unit.test/voice/status")),
            HttpMethod.Post
          ),
          CallbackUrlOptionalAndRequiredMethod(
            Some(CallbackUrl.VoiceUrl("http://unit.test/voice/url")),
            HttpMethod.Post
          ),
          true,
          true,
          true,
          Some(ByocTrunk.Sid.unsafe("BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")),
          Some(TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"))
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipDomainCreateRequestExecutor =
          TwilioClient.defaultImpl().voice.sipDomainCreate
        val resultFut: Future[
          Either[SipDomainCreateRequestExecutor.SipDomainCreateException, SipDomain]
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

  private def twilioError21231 =
    """{
      |  "code": 21231,
      |  "message": "Invalid Domain Name",
      |  "more_info": "https://www.twilio.com/docs/errors/21231",
      |  "status": 400
      |}""".stripMargin

  private def twilioError21232 =
    """{
      |  "code": 21232,
      |  "message": "Cannot create subdomain because the parent domain does not exist",
      |  "more_info": "https://www.twilio.com/docs/errors/21232",
      |  "status": 400
      |}""".stripMargin

  private def twilioResponse1 =
    s"""{
       |  "account_sid": "${CommonFixtures.accountSid1}",
       |  "api_version": "2010-04-01",
       |  "auth_type": "IP_ACL",
       |  "date_created": "Mon, 20 Jul 2015 17:27:10 +0000",
       |  "date_updated": "Mon, 20 Jul 2015 17:27:10 +0000",
       |  "domain_name": "unitTest.sip.twilio.com",
       |  "friendly_name": "Unit test domain name",
       |  "sip_registration": true,
       |  "sid": "SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "subresource_uris": {
       |    "credential_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/CredentialListMappings.json",
       |    "ip_access_control_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAccessControlListMappings.json"
       |  },
       |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |  "voice_fallback_method": "POST",
       |  "voice_fallback_url": "http://unit.test/voice/fallback",
       |  "voice_method": "POST",
       |  "voice_status_callback_method": "POST",
       |  "voice_status_callback_url": "http://unit.test/voice/status",
       |  "voice_url": "http://unit.test/voice/url",
       |  "emergency_calling_enabled": true,
       |  "secure": true,
       |  "byoc_trunk_sid": "BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |  "emergency_caller_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"
       |}
       |""".stripMargin
}
