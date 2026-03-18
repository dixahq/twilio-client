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

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.SipDomainReadRequestExecutor
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{ByocTrunk, SipDomain}
import com.dixa.twilio.model.{CallbackUrlOptionalAndRequiredMethod, HttpMethod}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, absent, equalTo}

import java.time.{ZoneOffset, ZonedDateTime}

final class SipDomainReadTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to read all SipDomains" should {
      "returned all of Twilios paginated results as a stream" in {

        val request = SipDomainReadRequestExecutor.SipDomainReadRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json"
              )
            )
            .withQueryParam("PageToken", absent())
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json"
              )
            )
            .withQueryParam("PageToken", equalTo(nextPageToken))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse2)
            )
        )

        val expected = Seq(
          SipDomain(
            CommonFixtures.accountSid1,
            Some(SipDomain.AuthType.IpAcl),
            ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
            ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
            SipDomain.DomainName.unsafe("unitTest.sip.twilio.com"),
            Some(SipDomain.FriendlyName.unsafe("Unit test domain name")),
            SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
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
          ),
          SipDomain(
            CommonFixtures.accountSid1,
            Some(SipDomain.AuthType.CredentialList),
            ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
            ZonedDateTime.of(2015, 7, 20, 17, 27, 10, 0, ZoneOffset.UTC).toInstant,
            SipDomain.DomainName.unsafe("unitTest2.sip.twilio.com"),
            Some(SipDomain.FriendlyName.unsafe("Unit test domain name 2")),
            SipDomain.Sid.unsafe("SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
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
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: SipDomainReadRequestExecutor =
          TwilioClient.defaultImpl().voice.sipDomainRead
        val resultSource: Source[
          Either[SipDomainReadRequestExecutor.SipDomainReadException, SipDomain],
          NotUsed
        ] = {
          instance.source(connSettings, request)
        }
        resultSource.runWith(Sink.seq).map { result =>
          val succResult = result.map(subResult =>
            subResult.getOrElse {
              val e = subResult.left.getOrElse(fail("No success or either, how can that happen :D"))
              fail("expected successfully result here", e)
            }
          )
          assert(succResult === expected)
        }
      }
    }
  }

  def nextPageToken                      = "PAAP43d194ab52aefee77ec51e4e185f3e38"
  private def twilioResponse1NextPageUri =
    s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json?PageSize=1&Page=1&PageToken=$nextPageToken"

  private def twilioResponse1 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SipDomains.json?PageSize=1&Page=0",
       |  "end": 0,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json?PageSize=1&Page=0",
       |  "page_size": 1,
       |  "page": 0,
       |  "domains": [
       |    {
       |       "account_sid": "${CommonFixtures.accountSid1}",
       |       "api_version": "2010-04-01",
       |       "auth_type": "IP_ACL",
       |       "date_created": "Mon, 20 Jul 2015 17:27:10 +0000",
       |       "date_updated": "Mon, 20 Jul 2015 17:27:10 +0000",
       |       "domain_name": "unitTest.sip.twilio.com",
       |       "friendly_name": "Unit test domain name",
       |       "sip_registration": true,
       |       "sid": "SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |       "subresource_uris": {
       |         "credential_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/CredentialListMappings.json",
       |         "ip_access_control_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAccessControlListMappings.json"
       |       },
       |       "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |       "voice_fallback_method": "POST",
       |       "voice_fallback_url": "http://unit.test/voice/fallback",
       |       "voice_method": "POST",
       |       "voice_status_callback_method": "POST",
       |       "voice_status_callback_url": "http://unit.test/voice/status",
       |       "voice_url": "http://unit.test/voice/url",
       |       "emergency_calling_enabled": true,
       |       "secure": true,
       |       "byoc_trunk_sid": "BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |       "emergency_caller_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"
       |     }
       |  ],
       |  "next_page_uri": "$twilioResponse1NextPageUri",
       |  "start": 0
       |}
       |""".stripMargin

  private def twilioResponse2 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json?PageSize=1&Page=0",
       |  "end": 1,
       |  "previous_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/SIP/Domains.json?PageSize=1&Page=0&PageToken=PBAP33f1531a9dadcd439bb8d2f06b0ebd1f",
       |  "uri": "$twilioResponse1NextPageUri",
       |  "page_size": 1,
       |  "page": 1,
       |  "domains": [
       |    {
       |       "account_sid": "${CommonFixtures.accountSid1}",
       |       "api_version": "2010-04-01",
       |       "auth_type": "CREDENTIAL_LIST",
       |       "date_created": "Mon, 20 Jul 2015 17:27:10 +0000",
       |       "date_updated": "Mon, 20 Jul 2015 17:27:10 +0000",
       |       "domain_name": "unitTest2.sip.twilio.com",
       |       "friendly_name": "Unit test domain name 2",
       |       "sip_registration": true,
       |       "sid": "SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
       |       "subresource_uris": {
       |         "credential_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/CredentialListMappings.json",
       |         "ip_access_control_list_mappings": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IpAccessControlListMappings.json"
       |       },
       |       "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/SIP/Domains/SDXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |       "voice_fallback_method": "POST",
       |       "voice_fallback_url": "http://unit.test/voice/fallback",
       |       "voice_method": "POST",
       |       "voice_status_callback_method": "POST",
       |       "voice_status_callback_url": "http://unit.test/voice/status",
       |       "voice_url": "http://unit.test/voice/url",
       |       "emergency_calling_enabled": true,
       |       "secure": true,
       |       "byoc_trunk_sid": "BYXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
       |       "emergency_caller_sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"
       |     }
       |  ],
       |  "next_page_uri": null,
       |  "start": 1
       |}
       |""".stripMargin

}
