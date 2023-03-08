package com.dixa.twilio.client.twilioClient.phonenumber

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor.{
  IncomingNumbersReadException,
  IncomingNumbersReadRequest
}
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary
import com.dixa.twilio.model.phonenumber.{
  PhoneNumberE164,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumberSid
}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

final class IncomingPhoneNumberListTest extends TwilioClientTest {
  classOf[TwilioClientPhoneNumber].getSimpleName when {

    "asked to list incoming phone numbers" should {

      "delegate safely to twilio and apply supplied text filter" in {

        wireMockServer.stubFor(
          WireMock
            .get(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json"
              )
            )
            .withQueryParam("PhoneNumber", WireMock.equalTo("+45"))
            .withQueryParam("PageSize", WireMock.equalTo(expectedPageSizeForStreams))
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
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json"
              )
            )
            .withQueryParam("PhoneNumber", WireMock.equalTo("+45"))
            .withQueryParam("PageSize", WireMock.equalTo(expectedPageSizeForStreams))
            .withQueryParam("Page", WireMock.equalTo("1"))
            .withQueryParam("PageToken", WireMock.equalTo("PAID6e403001f47c7500d77c5d5e3713d8cb"))
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(twilioResponse2)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber
        val req = IncomingNumbersReadRequest(
          Some(TwilioIncomingPhoneNumber.PhoneNumberFilter("+45"))
        )
        val resultSource
            : Source[Either[IncomingNumbersReadException, TwilioIncomingPhoneNumber], NotUsed] =
          instance.incomingPhoneNumberList.source(
            twilioConnectionSetting,
            req
          )
        val resultFut =
          resultSource.runWith(Sink.seq)

        val expected = Seq(
          TwilioIncomingPhoneNumber(
            TwilioPhoneNumberSid("PNf691901a0361ccfb5e4c11dc073a7274"),
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            TwilioIncomingPhoneNumber.FriendlyName("(459) 375-1435"),
            PhoneNumberE164("+4593751435"),
            PhoneNumberCapabilitiesSummary(voice = true, sms = true, mms = false, fax = false)
          ),
          TwilioIncomingPhoneNumber(
            TwilioPhoneNumberSid("PNa6ab2f33d0ffca5a3fa907a4ce302607"),
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            TwilioIncomingPhoneNumber.FriendlyName("uva_testing_sms_dk"),
            PhoneNumberE164("+4581827622"),
            PhoneNumberCapabilitiesSummary(voice = true, sms = true, mms = true, fax = true)
          ),
          TwilioIncomingPhoneNumber(
            TwilioPhoneNumberSid("PN8ac53dd1867205c550ee4d41a35c0896"),
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            TwilioIncomingPhoneNumber.FriendlyName("STAGING-2 NUMBER"),
            PhoneNumberE164("+4578750614"),
            PhoneNumberCapabilitiesSummary(voice = true, sms = false, mms = false, fax = false)
          )
        )

        resultFut.map(res => assert(res === expected.map(Right(_))))
      }
    }
  }

  //format: off
  private def twilioResponse1 =
    s"""{
      |  "first_page_uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=0",
      |  "end": 1,
      |  "previous_page_uri": null,
      |  "incoming_phone_numbers": [
      |    {
      |      "sid": "PNf691901a0361ccfb5e4c11dc073a7274",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "(459) 375-1435",
      |      "phone_number": "+4593751435",
      |      "voice_url": "https://demo.twilio.com/welcome/voice/",
      |      "voice_method": "POST",
      |      "voice_fallback_url": "",
      |      "voice_fallback_method": "POST",
      |      "voice_caller_id_lookup": false,
      |      "date_created": "Thu, 27 Jan 2022 11:58:21 +0000",
      |      "date_updated": "Mon, 31 Jan 2022 16:43:48 +0000",
      |      "sms_url": "https://sms-twilio.euw1.stag.dixa.io/v1/e7a04fc4-bba8-48a8-a92e-013606a188a6/sms",
      |      "sms_method": "POST",
      |      "sms_fallback_url": "",
      |      "sms_fallback_method": "POST",
      |      "address_requirements": "local",
      |      "beta": false,
      |      "capabilities": {
      |        "voice": true,
      |        "sms": true,
      |        "mms": false,
      |        "fax": false
      |      },
      |      "status_callback": "",
      |      "status_callback_method": "POST",
      |      "api_version": "2010-04-01",
      |      "voice_application_sid": "",
      |      "sms_application_sid": "",
      |      "origin": "twilio",
      |      "trunk_sid": null,
      |      "emergency_status": "Inactive",
      |      "emergency_address_sid": null,
      |      "emergency_address_status": "unregistered",
      |      "address_sid": "ADdcdaa0cc98f4706187c4dba5f37fc5e3",
      |      "identity_sid": null,
      |      "bundle_sid": null,
      |      "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/IncomingPhoneNumbers/PNf691901a0361ccfb5e4c11dc073a7274.json",
      |      "status": "in-use"
      |    },
      |    {
      |      "sid": "PNa6ab2f33d0ffca5a3fa907a4ce302607",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "uva_testing_sms_dk",
      |      "phone_number": "+4581827622",
      |      "voice_url": "https://demo.twilio.com/welcome/voice/",
      |      "voice_method": "POST",
      |      "voice_fallback_url": "",
      |      "voice_fallback_method": "POST",
      |      "voice_caller_id_lookup": false,
      |      "date_created": "Tue, 02 Nov 2021 10:44:40 +0000",
      |      "date_updated": "Mon, 10 Jan 2022 14:53:10 +0000",
      |      "sms_url": "https://twilio-hooks.sms.stag.dixa.io/receiveSms",
      |      "sms_method": "POST",
      |      "sms_fallback_url": "",
      |      "sms_fallback_method": "POST",
      |      "address_requirements": "local",
      |      "beta": false,
      |      "capabilities": {
      |        "voice": true,
      |        "sms": true,
      |        "mms": true,
      |        "fax": true
      |      },
      |      "status_callback": "",
      |      "status_callback_method": "POST",
      |      "api_version": "2010-04-01",
      |      "voice_application_sid": "",
      |      "sms_application_sid": "",
      |      "origin": "twilio",
      |      "trunk_sid": null,
      |      "emergency_status": "Inactive",
      |      "emergency_address_sid": null,
      |      "emergency_address_status": "unregistered",
      |      "address_sid": "ADdcdaa0cc98f4706187c4dba5f37fc5e3",
      |      "identity_sid": null,
      |      "bundle_sid": null,
      |      "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/IncomingPhoneNumbers/PNa6ab2f33d0ffca5a3fa907a4ce302607.json",
      |      "status": "in-use"
      |    }
      |  ],
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=0",
      |  "page_size": $expectedPageSizeForStreams,
      |  "start": 0,
      |  "next_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=1&PageToken=PAID6e403001f47c7500d77c5d5e3713d8cb",
      |  "page": 0
      |}
      |""".stripMargin

  private def twilioResponse2 =
    s"""{
      |  "first_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=0",
      |  "end": 2,
      |  "previous_page_uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=0&PageToken=PBIDad091ef5dec23d92ffd458beee727945",
      |  "incoming_phone_numbers": [
      |    {
      |      "sid": "PN8ac53dd1867205c550ee4d41a35c0896",
      |      "account_sid": "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
      |      "friendly_name": "STAGING-2 NUMBER",
      |      "phone_number": "+4578750614",
      |      "voice_url": "https://twilio.euw1.stag2.dixa.io/v1/twilio/incoming",
      |      "voice_method": "POST",
      |      "voice_fallback_url": "",
      |      "voice_fallback_method": "POST",
      |      "voice_caller_id_lookup": false,
      |      "date_created": "Tue, 16 Oct 2018 09:11:07 +0000",
      |      "date_updated": "Wed, 21 Apr 2021 11:19:04 +0000",
      |      "sms_url": "",
      |      "sms_method": "POST",
      |      "sms_fallback_url": "",
      |      "sms_fallback_method": "POST",
      |      "address_requirements": "local",
      |      "beta": false,
      |      "capabilities": {
      |        "voice": true,
      |        "sms": false,
      |        "mms": false,
      |        "fax": false
      |      },
      |      "status_callback": "https://twilio.euw1.stag2.dixa.io/v1/twilio/completed",
      |      "status_callback_method": "POST",
      |      "api_version": "2010-04-01",
      |      "voice_application_sid": "",
      |      "sms_application_sid": "",
      |      "origin": "twilio",
      |      "trunk_sid": null,
      |      "emergency_status": "Inactive",
      |      "emergency_address_sid": null,
      |      "emergency_address_status": "unregistered",
      |      "address_sid": "ADdaea97e8e8cd170eed7e8f3f1691d34e",
      |      "identity_sid": null,
      |      "bundle_sid": null,
      |      "uri": "/2010-04-01/Accounts/ACf6c9aa4f8756c258be45a6d2637cfa15/IncomingPhoneNumbers/PN8ac53dd1867205c550ee4d41a35c0896.json",
      |      "status": "in-use"
      |    }
      |  ],
      |  "uri": "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers.json?PhoneNumber=%2B45&PageSize=$expectedPageSizeForStreams&Page=1&PageToken=PAID6e403001f47c7500d77c5d5e3713d8cb",
      |  "page_size": $expectedPageSizeForStreams,
      |  "start": 2,
      |  "next_page_uri": null,
      |  "page": 1
      |}
      |""".stripMargin
  //format: on    

}
