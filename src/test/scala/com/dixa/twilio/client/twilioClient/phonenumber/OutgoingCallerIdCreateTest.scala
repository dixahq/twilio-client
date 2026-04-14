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

import com.dixa.twilio.client.phonenumber.OutgoingCallerIdCreateRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdCreateRequestExecutor.CallDelay
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.TwilioClientVoice
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfString.DtmfStringElement.WaitElement
import com.dixa.twilio.model.dtmf.{DtmfDigit, DtmfString}
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}
import com.dixa.twilio.model.voice.Call
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class OutgoingCallerIdCreateTest extends TwilioClientTest {
  classOf[TwilioClientVoice].getSimpleName when {

    "is asked to create a OutgoindCallerId" should {
      "ask twilio to send it, and return the OutgoingCallerId it gets back from Twilio" in {

        val phonenumber  = PhoneNumberE164.unsafe("+4588888888")
        val friendlyName = OutgoingCallerId.FriendlyName.constructInstance("A new service")
        val callDelay    = CallDelay.Seconds26
        val extension    = DtmfString(DtmfDigit.`1`, WaitElement, DtmfDigit.`2`, DtmfDigit.`4`)
        val callback     = CallbackUrl.OutgoingCallerIdVerificationUrl("test.i/test")
        val createReq = OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .withPhoneNumber(phonenumber)
            .withFriendlyName(friendlyName)
            .withCallDelay(callDelay)
            .withExtension(extension)
            .withCallback(callback)
            .withCallbackMethod(HttpMethod.Post)
            .build()
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())

        val expectedPath = s"/2010-04-01/Accounts/${connSettings.accountSid}/OutgoingCallerIds.json"

        wireMockServer.stubFor(
          WireMock
            .post(
              WireMock.urlEqualTo(expectedPath)
            )
            .withFormParam("PhoneNumber", WireMock.equalTo(createReq.phoneNumber.twilioString))
            .withFormParam(
              "FriendlyName",
              WireMock.equalTo(createReq.friendlyName.map(_.twilioString).getOrElse(""))
            )
            .withFormParam(
              "CallDelay",
              WireMock.equalTo(createReq.callDelay.map(_.twilioString).getOrElse(""))
            )
            .withFormParam(
              "Extension",
              WireMock.equalTo(createReq.extension.map(_.twilioString).getOrElse(""))
            )
            .withFormParam(
              "StatusCallback",
              WireMock.equalTo(createReq.statusCallback.map(_.twilioString).getOrElse(""))
            )
            .withFormParam(
              "StatusCallbackMethod",
              WireMock.equalTo(createReq.statusCallbackMethod.map(_.twilioString).getOrElse(""))
            )
            .withBasicAuth(
              connSettings.accountSid.twilioString,
              TwilioTestConstants.authToken.asString
            )
            .willReturn(
              aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                  s"""{
                     |  "account_sid": "${TwilioTestConstants.accountSid.twilioString}",
                     |  "call_sid": "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                     |  "friendly_name": "${friendlyName.twilioString}",
                     |  "phone_number": "${phonenumber.twilioString}",
                     |  "validation_code": "11111"
                     |}""".stripMargin
                )
            )
        )

        val expected = OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse(
          accountSid = createReq.accountSid,
          friendlyName = Some(friendlyName),
          phoneNumber = createReq.phoneNumber,
          validationCode =
            DtmfString(DtmfDigit.`1`, DtmfDigit.`1`, DtmfDigit.`1`, DtmfDigit.`1`, DtmfDigit.`1`),
          callSid = Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        )

        val instance = TwilioClient.defaultImpl().phoneNumber
        val resultFut: Future[Either[
          OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException,
          OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse
        ]] =
          instance.outgoingCallerIdCreate.run(connSettings, createReq)
        resultFut.map(result => assert(result === Right(expected)))
      }
    }
  }
}
