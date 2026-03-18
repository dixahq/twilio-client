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

package com.dixa.twilio.client.twilioClient.general

import org.apache.pekko.NotUsed
import org.apache.pekko.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.ApplicationReadRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.Application
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, absent, equalTo}

import java.net.URL
import java.time.{ZoneOffset, ZonedDateTime}

final class ApplicationReadTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to read all Applications" should {
      "returned all of Twilios paginated results as a stream" in {

        val request = ApplicationReadRequestExecutor.ApplicationReadRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json"
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
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json"
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
          Application(
            accountSid = CommonFixtures.accountSid1,
            dateCreated = ZonedDateTime.of(2011, 8, 22, 20, 59, 45, 0, ZoneOffset.UTC).toInstant,
            dateUpdated = ZonedDateTime.of(2015, 8, 18, 16, 48, 57, 0, ZoneOffset.UTC).toInstant,
            friendlyName = Some(Application.FriendlyName.unsafe("Phone Me")),
            messageStatusCallback = Some(
              CallbackUrl.MessageStatusCallback(
                new URL("http://www.example.com/sms-status-callback")
              )
            ),
            sid = Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX254"),
            smsFallbackMethod = HttpMethod.Get,
            smsFallbackUrl =
              Some(CallbackUrl.SmsFallbackUrl("http://www.example.com/sms-fallback")),
            smsMethod = HttpMethod.Get,
            smsStatusCallback =
              Some(CallbackUrl.SmsStatusCallback("http://www.example.com/sms-status-callback")),
            smsUrl = Some(CallbackUrl.SmsUrl("http://example.com")),
            statusCallback = Some(CallbackUrl.ApplicationStatusCallback("http://example.com")),
            statusCallbackMethod = HttpMethod.Get,
            voiceCallerIdLookup = false,
            voiceFallbackMethod = HttpMethod.Get,
            voiceFallbackUrl =
              Some(CallbackUrl.VoiceFallbackUrl("http://www.example.com/voice-callback")),
            voiceMethod = HttpMethod.Get,
            voiceUrl = Some(CallbackUrl.VoiceUrl("http://demo.twilio.com/docs/voice.xml")),
            publicApplicationConnectEnabled = true
          ),
          Application(
            accountSid = CommonFixtures.accountSid1,
            dateCreated = ZonedDateTime.of(2011, 8, 22, 20, 59, 45, 0, ZoneOffset.UTC).toInstant,
            dateUpdated = ZonedDateTime.of(2015, 8, 18, 16, 48, 57, 0, ZoneOffset.UTC).toInstant,
            friendlyName = Some(Application.FriendlyName.unsafe("Phone Me")),
            messageStatusCallback = Some(
              CallbackUrl.MessageStatusCallback(
                new URL("http://www.example.com/sms-status-callback")
              )
            ),
            sid = Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX876"),
            smsFallbackMethod = HttpMethod.Get,
            smsFallbackUrl =
              Some(CallbackUrl.SmsFallbackUrl("http://www.example.com/sms-fallback")),
            smsMethod = HttpMethod.Get,
            smsStatusCallback =
              Some(CallbackUrl.SmsStatusCallback("http://www.example.com/sms-status-callback")),
            smsUrl = Some(CallbackUrl.SmsUrl("http://example.com")),
            statusCallback = Some(CallbackUrl.ApplicationStatusCallback("http://example.com")),
            statusCallbackMethod = HttpMethod.Get,
            voiceCallerIdLookup = false,
            voiceFallbackMethod = HttpMethod.Get,
            voiceFallbackUrl =
              Some(CallbackUrl.VoiceFallbackUrl("http://www.example.com/voice-callback")),
            voiceMethod = HttpMethod.Get,
            voiceUrl = Some(CallbackUrl.VoiceUrl("http://demo.twilio.com/docs/voice.xml")),
            publicApplicationConnectEnabled = true
          )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ApplicationReadRequestExecutor =
          TwilioClient.defaultImpl().general.applicationRead
        val resultSource: Source[
          Either[ApplicationReadRequestExecutor.ApplicationReadException, Application],
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

      "Only return the single application matching the friendly name, if one such was supplied" in {
        val friendlyName = Application.FriendlyName.unsafe("Phone Me")
        val request      = ApplicationReadRequestExecutor.ApplicationReadRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withFriendlyName(friendlyName)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json"
              )
            )
            .withQueryParam("PageToken", absent())
            .withQueryParam("FriendlyName", equalTo("Phone Me"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponseFriendlyNameMatch)
            )
        )

        val expected = Seq(
          Application(
            accountSid = CommonFixtures.accountSid1,
            dateCreated = ZonedDateTime.of(2011, 8, 22, 20, 59, 45, 0, ZoneOffset.UTC).toInstant,
            dateUpdated = ZonedDateTime.of(2015, 8, 18, 16, 48, 57, 0, ZoneOffset.UTC).toInstant,
            friendlyName = Some(friendlyName),
            messageStatusCallback = Some(
              CallbackUrl.MessageStatusCallback(
                new URL("http://www.example.com/sms-status-callback")
              )
            ),
            sid = Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX254"),
            smsFallbackMethod = HttpMethod.Get,
            smsFallbackUrl =
              Some(CallbackUrl.SmsFallbackUrl("http://www.example.com/sms-fallback")),
            smsMethod = HttpMethod.Get,
            smsStatusCallback =
              Some(CallbackUrl.SmsStatusCallback("http://www.example.com/sms-status-callback")),
            smsUrl = Some(CallbackUrl.SmsUrl("http://example.com")),
            statusCallback = Some(CallbackUrl.ApplicationStatusCallback("http://example.com")),
            statusCallbackMethod = HttpMethod.Get,
            voiceCallerIdLookup = false,
            voiceFallbackMethod = HttpMethod.Get,
            voiceFallbackUrl =
              Some(CallbackUrl.VoiceFallbackUrl("http://www.example.com/voice-callback")),
            voiceMethod = HttpMethod.Get,
            voiceUrl = Some(CallbackUrl.VoiceUrl("http://demo.twilio.com/docs/voice.xml")),
            publicApplicationConnectEnabled = true
          )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ApplicationReadRequestExecutor =
          TwilioClient.defaultImpl().general.applicationRead
        val resultSource: Source[
          Either[ApplicationReadRequestExecutor.ApplicationReadException, Application],
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
    s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=1&PageToken=$nextPageToken"

  private def twilioResponse1 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0",
       |  "end": 0,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0",
       |  "page_size": 1,
       |  "page": 0,
       |  "applications": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "api_version": "2010-04-01",
       |      "date_created": "Mon, 22 Aug 2011 20:59:45 +0000",
       |      "date_updated": "Tue, 18 Aug 2015 16:48:57 +0000",
       |      "friendly_name": "Phone Me",
       |      "message_status_callback": "http://www.example.com/sms-status-callback",
       |      "sid": "APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX254",
       |      "sms_fallback_method": "GET",
       |      "sms_fallback_url": "http://www.example.com/sms-fallback",
       |      "sms_method": "GET",
       |      "sms_status_callback": "http://www.example.com/sms-status-callback",
       |      "sms_url": "http://example.com",
       |      "status_callback": "http://example.com",
       |      "status_callback_method": "GET",
       |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |      "voice_caller_id_lookup": false,
       |      "voice_fallback_method": "GET",
       |      "voice_fallback_url": "http://www.example.com/voice-callback",
       |      "voice_method": "GET",
       |      "voice_url": "http://demo.twilio.com/docs/voice.xml",
       |      "public_application_connect_enabled": true
       |    }
       |  ],
       |  "next_page_uri": "$twilioResponse1NextPageUri",
       |  "start": 0
       |}
       |""".stripMargin

  private def twilioResponse2 =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0",
       |  "end": 1,
       |  "previous_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0&PageToken=PBAP33f1531a9dadcd439bb8d2f06b0ebd1f",
       |  "uri": "$twilioResponse1NextPageUri",
       |  "page_size": 1,
       |  "page": 1,
       |  "applications": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "api_version": "2010-04-01",
       |      "date_created": "Mon, 22 Aug 2011 20:59:45 +0000",
       |      "date_updated": "Tue, 18 Aug 2015 16:48:57 +0000",
       |      "friendly_name": "Phone Me",
       |      "message_status_callback": "http://www.example.com/sms-status-callback",
       |      "sid": "APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX876",
       |      "sms_fallback_method": "GET",
       |      "sms_fallback_url": "http://www.example.com/sms-fallback",
       |      "sms_method": "GET",
       |      "sms_status_callback": "http://www.example.com/sms-status-callback",
       |      "sms_url": "http://example.com",
       |      "status_callback": "http://example.com",
       |      "status_callback_method": "GET",
       |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |      "voice_caller_id_lookup": false,
       |      "voice_fallback_method": "GET",
       |      "voice_fallback_url": "http://www.example.com/voice-callback",
       |      "voice_method": "GET",
       |      "voice_url": "http://demo.twilio.com/docs/voice.xml",
       |      "public_application_connect_enabled": true
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "start": 1
       |}
       |""".stripMargin

  private def twilioResponseFriendlyNameMatch =
    s"""{
       |  "first_page_uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0",
       |  "end": 0,
       |  "previous_page_uri": null,
       |  "uri": "/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json?PageSize=1&Page=0",
       |  "page_size": 1,
       |  "page": 0,
       |  "applications": [
       |    {
       |      "account_sid": "${CommonFixtures.accountSid1}",
       |      "api_version": "2010-04-01",
       |      "date_created": "Mon, 22 Aug 2011 20:59:45 +0000",
       |      "date_updated": "Tue, 18 Aug 2015 16:48:57 +0000",
       |      "friendly_name": "Phone Me",
       |      "message_status_callback": "http://www.example.com/sms-status-callback",
       |      "sid": "APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX254",
       |      "sms_fallback_method": "GET",
       |      "sms_fallback_url": "http://www.example.com/sms-fallback",
       |      "sms_method": "GET",
       |      "sms_status_callback": "http://www.example.com/sms-status-callback",
       |      "sms_url": "http://example.com",
       |      "status_callback": "http://example.com",
       |      "status_callback_method": "GET",
       |      "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |      "voice_caller_id_lookup": false,
       |      "voice_fallback_method": "GET",
       |      "voice_fallback_url": "http://www.example.com/voice-callback",
       |      "voice_method": "GET",
       |      "voice_url": "http://demo.twilio.com/docs/voice.xml",
       |      "public_application_connect_enabled": true
       |    }
       |  ],
       |  "next_page_uri": null,
       |  "start": 0
       |}
       |""".stripMargin

}
