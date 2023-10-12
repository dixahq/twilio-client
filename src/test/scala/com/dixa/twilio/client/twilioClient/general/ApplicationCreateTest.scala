package com.dixa.twilio.client.twilioClient.general

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.ApplicationCreateRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.Application
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import java.net.URL
import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future

final class ApplicationCreateTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to create an Application" should {
      "ask twilio to create it, and return the Service it gets back from Twilio" in {

        val request = ApplicationCreateRequestExecutor.ApplicationCreateRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withVoiceUrl(CallbackUrl.VoiceUrl("http://demo.twilio.com/docs/voice.xml"))
            .withVoiceMethod(HttpMethod.Get)
            .withVoiceFallbackUrl(
              CallbackUrl.VoiceFallbackUrl("http://www.example.com/voice-callback")
            )
            .withVoiceFallbackMethod(HttpMethod.Get)
            .withStatusCallback(CallbackUrl.ApplicationStatusCallback("http://example.com"))
            .withStatusCallbackMethod(HttpMethod.Get)
            .withVoiceCallerIdLookup(false)
            .withSmsUrl(CallbackUrl.SmsUrl("http://example.com"))
            .withSmsMethod(HttpMethod.Get)
            .withSmsFallbackUrl(CallbackUrl.SmsFallbackUrl("http://www.example.com/sms-fallback"))
            .withSmsFallbackMethod(HttpMethod.Get)
            .withSmsStatusCallback(
              CallbackUrl.SmsStatusCallback("http://www.example.com/sms-status-callback")
            )
            .withMessageStatusCallback(
              CallbackUrl.MessageStatusCallback(
                new URL("http://www.example.com/sms-status-callback")
              )
            )
            .withFriendlyName(Application.FriendlyName("Phone Me"))
            .withPublicApplicationConnectEnabled(true)
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications.json"
              )
            )
            .withRequestBody(
              WireMock.containing("VoiceUrl=http%3A%2F%2Fdemo.twilio.com%2Fdocs%2Fvoice.xml")
            )
            .withRequestBody(WireMock.containing("VoiceMethod=GET"))
            .withRequestBody(
              WireMock.containing("VoiceFallbackUrl=http%3A%2F%2Fwww.example.com%2Fvoice-callback")
            )
            .withRequestBody(WireMock.containing(s"VoiceFallbackMethod=GET"))
            .withRequestBody(WireMock.containing(s"StatusCallback=http%3A%2F%2Fexample.com"))
            .withRequestBody(WireMock.containing(s"StatusCallbackMethod=GET"))
            .withRequestBody(WireMock.containing(s"VoiceCallerIdLookup=false"))
            .withRequestBody(WireMock.containing(s"SmsUrl=http%3A%2F%2Fexample.com"))
            .withRequestBody(WireMock.containing(s"SmsMethod=GET"))
            .withRequestBody(
              WireMock.containing(s"SmsFallbackUrl=http%3A%2F%2Fwww.example.com%2Fsms-fallback")
            )
            .withRequestBody(WireMock.containing(s"SmsFallbackMethod=GET"))
            .withRequestBody(
              WireMock.containing(
                s"SmsStatusCallback=http%3A%2F%2Fwww.example.com%2Fsms-status-callback"
              )
            )
            .withRequestBody(
              WireMock.containing(
                s"MessageStatusCallback=http%3A%2F%2Fwww.example.com%2Fsms-status-callback"
              )
            )
            .withRequestBody(WireMock.containing(s"FriendlyName=Phone+Me"))
            .withRequestBody(WireMock.containing(s"PublicApplicationConnectEnabled=true"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
            .willReturn(
              aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val expected = Application(
          accountSid = CommonFixtures.accountSid1,
          dateCreated = ZonedDateTime.of(2011, 8, 22, 20, 59, 45, 0, ZoneOffset.UTC).toInstant,
          dateUpdated = ZonedDateTime.of(2015, 8, 18, 16, 48, 57, 0, ZoneOffset.UTC).toInstant,
          friendlyName = Some(Application.FriendlyName("Phone Me")),
          messageStatusCallback = Some(
            CallbackUrl.MessageStatusCallback(new URL("http://www.example.com/sms-status-callback"))
          ),
          sid = Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
          smsFallbackMethod = HttpMethod.Get,
          smsFallbackUrl = Some(CallbackUrl.SmsFallbackUrl("http://www.example.com/sms-fallback")),
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

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ApplicationCreateRequestExecutor =
          TwilioClient.defaultImpl().general.applicationCreate
        val resultFut: Future[
          Either[ApplicationCreateRequestExecutor.ApplicationCreateException, Application]
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
       |  "api_version": "2010-04-01",
       |  "date_created": "Mon, 22 Aug 2011 20:59:45 +0000",
       |  "date_updated": "Tue, 18 Aug 2015 16:48:57 +0000",
       |  "friendly_name": "Phone Me",
       |  "message_status_callback": "http://www.example.com/sms-status-callback",
       |  "sid": "APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
       |  "sms_fallback_method": "GET",
       |  "sms_fallback_url": "http://www.example.com/sms-fallback",
       |  "sms_method": "GET",
       |  "sms_status_callback": "http://www.example.com/sms-status-callback",
       |  "sms_url": "http://example.com",
       |  "status_callback": "http://example.com",
       |  "status_callback_method": "GET",
       |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json",
       |  "voice_caller_id_lookup": false,
       |  "voice_fallback_method": "GET",
       |  "voice_fallback_url": "http://www.example.com/voice-callback",
       |  "voice_method": "GET",
       |  "voice_url": "http://demo.twilio.com/docs/voice.xml",
       |  "public_application_connect_enabled": true
       |}
       |""".stripMargin
}
