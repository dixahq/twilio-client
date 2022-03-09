package com.dixa.twilio.client.twilioClient.phonenumber

import akka.stream.scaladsl.Sink
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.PhoneNumberCapabilities._
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement.AddressRequirementType
import com.dixa.twilio.model.phonenumber._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.neovisionaries.i18n.CountryCode
import org.scalatest.matchers.should.Matchers

final class ActivePhoneNumberListTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientMessaging].getSimpleName when {

    "activePhoneNumberList" should {
      "lists a single number when filter applied" in {
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo("/Numbers/ActiveNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber

        val resultFut = instance
          .activePhoneNumberList(
            twilioConnectionSetting,
            Some(TwilioPhoneNumberSid("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"))
          )
          .runWith(Sink.seq)

        val expected = Seq(
          genAvailableNumber("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
        )

        resultFut.map(res => res.toSet shouldBe expected.toSet)
      }

      "lists multiple pages of numbers" in {
        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo("/Numbers/ActiveNumbers/")
            )
            .withQueryParam("PageSize", WireMock.equalTo(expectedPageSizeForStreams))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse2)
            )
        )

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo("/Numbers/ActiveNumbers/")
            )
            .withQueryParam("PageSize", WireMock.equalTo(expectedPageSizeForStreams))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse1)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber

        val resultSource = instance.activePhoneNumberList(twilioConnectionSetting)
        val resultFut    = resultSource.runWith(Sink.seq)

        val expected = Seq(
          genAvailableNumber("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2"),
          genAvailableNumber("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1"),
        )

        resultFut.map(res => res.toSet shouldBe expected.toSet)
      }
    }
  }

  private def genAvailableNumber(pnSid: String) =
    TwilioActivePhoneNumber(
      TwilioPhoneNumberSid(pnSid),
      TwilioAccount.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
      PhoneNumberE164("+18559728742"),
      PhoneNumberType.TollFree,
      PhoneNumberLifecycle.GenerallyAvailable,
      PhoneNumberCapabilities(
        voice = VoiceCapabilities(
          inboundConnectivity = true,
          outboundConnectivity = true,
          e911 = false,
          fax = true,
          callsPerSecond = 20,
          concurrentCallsLimit = 40,
          longRecordLength = 30,
          inboundCalledDtmf = true,
          inboundCallerDtmf = true,
          sipTrunking = true,
          inboundCallerIdPreservation = CallerIdPreservation.International,
          inboundReachability = InboundReachability.Global,
        ),
        sms = SmsCapabilities(
          inboundConnectivity = true,
          outboundConnectivity = true,
          gsm7 = true,
          ucs2 = true,
          inboundSenderIdPreservation = CallerIdPreservation.International,
          inboundReachability = InboundReachability.Global,
          inboundMps = 10,
        ),
        mms = MmsCapabilities(
          inboundConnectivity = true,
          outboundConnectivity = true,
          inboundReachability = InboundReachability.Global,
          inboundMps = -1,
        )
      ),
      PhoneNumberRegulatoryRequirement(
        addressRequirement = AddressRequirementType.None
      ),
      PhoneNumberGeography(CountryCode.US),
    )

  //format: off
  private def twilioResponse1 =
    s"""{
      | "items": [
      |   {
      |     "phone_number": "+18559728742",
      |     "url": "https://preview.twilio.com/Numbers/ActiveNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |     "capabilities": {
      |       "voice": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "e911": false,
      |         "fax": true,
      |         "calls_per_second": 20,
      |         "concurrent_calls_limit": 40,
      |         "long_record_length": 30,
      |         "inbound_called_dtmf": true,
      |         "inbound_caller_dtmf": true,
      |         "sip_trunking": true,
      |         "inbound_caller_id_preservation": "international",
      |         "inbound_reachability": "global"
      |       },
      |       "sms": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "gsm7": true,
      |         "ucs2": true,
      |         "inbound_sender_id_preservation": "international",
      |         "inbound_reachability": "global",
      |         "inbound_mps": 10
      |       },
      |       "mms": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "inbound_reachability": "global",
      |         "inbound_mps": -1
      |       }
      |     },
      |     "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |     "sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1",
      |     "regulatory": {
      |         "address_requirements": "none"
      |     },
      |     "configuration": {
      |       "friendly_name": "(855) 972-8742",
      |       "status_callback_url": "",
      |       "status_callback_method": "POST",
      |       "voice": {
      |         "url": "",
      |         "method": "POST",
      |         "fallback_url": null,
      |         "fallback_method": "POST",
      |         "application_sid": null,
      |         "trunk_sid": null,
      |         "emergency_address_sid": null,
      |         "emergency_status": "Inactive",
      |         "caller_id_lookup": false
      |       },
      |       "sms": {
      |         "url": "",
      |         "method": "POST",
      |         "fallback_url": "",
      |         "fallback_method": "POST",
      |         "application_sid": ""
      |       }
      |     },
      |     "type": "tollfree",
      |     "lifecycle": "generally-available",
      |     "geography": {
      |       "iso_country": "US",
      |       "lata": null,
      |       "rate_center": null,
      |       "latitude": null,
      |       "longitude": null,
      |       "region": null,
      |       "locality": null,
      |       "postal_code": null
      |     }
      |   }
      | ],
      | "meta": {
      |   "page": 0,
      |   "page_size": 1000,
      |   "first_page_url": "http://localhost:${wireMockServer.port()}/Numbers/ActiveNumbers/?PageSize=1000&Page=1",
      |   "previous_page_url": null,
      |   "url": "http://localhost:${wireMockServer.port()}/Numbers/ActiveNumbers/?PageSize=1000&Page=1",
      |   "next_page_url": null,
      |   "key": "items"
      | }
      |}
      |""".stripMargin

  private def twilioResponse2 =
    s"""{
      | "items": [
      |   {
      |     "phone_number": "+18559728742",
      |     "url": "https://preview.twilio.com/Numbers/ActiveNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
      |     "capabilities": {
      |       "voice": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "e911": false,
      |         "fax": true,
      |         "calls_per_second": 20,
      |         "concurrent_calls_limit": 40,
      |         "long_record_length": 30,
      |         "inbound_called_dtmf": true,
      |         "inbound_caller_dtmf": true,
      |         "sip_trunking": true,
      |         "inbound_caller_id_preservation": "international",
      |         "inbound_reachability": "global"
      |       },
      |       "sms": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "gsm7": true,
      |         "ucs2": true,
      |         "inbound_sender_id_preservation": "international",
      |         "inbound_reachability": "global",
      |         "inbound_mps": 10
      |       },
      |       "mms": {
      |         "inbound_connectivity": true,
      |         "outbound_connectivity": true,
      |         "inbound_reachability": "global",
      |         "inbound_mps": -1
      |       }
      |     },
      |     "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |     "sid": "PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX2",
      |     "regulatory": {
      |         "address_requirements": "none"
      |     },
      |     "configuration": {
      |       "friendly_name": "(855) 972-8742",
      |       "status_callback_url": "",
      |       "status_callback_method": "POST",
      |       "voice": {
      |         "url": "",
      |         "method": "POST",
      |         "fallback_url": null,
      |         "fallback_method": "POST",
      |         "application_sid": null,
      |         "trunk_sid": null,
      |         "emergency_address_sid": null,
      |         "emergency_status": "Inactive",
      |         "caller_id_lookup": false
      |       },
      |       "sms": {
      |         "url": "",
      |         "method": "POST",
      |         "fallback_url": "",
      |         "fallback_method": "POST",
      |         "application_sid": ""
      |       }
      |     },
      |     "type": "tollfree",
      |     "lifecycle": "generally-available",
      |     "geography": {
      |       "iso_country": "US",
      |       "lata": null,
      |       "rate_center": null,
      |       "latitude": null,
      |       "longitude": null,
      |       "region": null,
      |       "locality": null,
      |       "postal_code": null
      |     }
      |   }
      | ],
      | "meta": {
      |   "page": 1,
      |   "page_size": 1000,
      |   "first_page_url": "http://localhost:${wireMockServer.port()}/Numbers/ActiveNumbers/?PageSize=1000&Page=0",
      |   "previous_page_url": null,
      |   "url": "http://localhost:${wireMockServer.port()}/Numbers/ActiveNumbers/?PageSize=1000&Page=0",
      |   "next_page_url": "http://localhost:${wireMockServer.port()}/Numbers/ActiveNumbers/?PageSize=1000&Page=1",
      |   "key": "items"
      | }
      |}
      |""".stripMargin
  //format: on    

}
