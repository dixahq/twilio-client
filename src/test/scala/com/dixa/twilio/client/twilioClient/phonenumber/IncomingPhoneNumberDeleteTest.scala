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

import org.apache.pekko.Done
import com.dixa.twilio.client.phonenumber.IncomingPhoneNumberDeleteRequestExecutor.{
  IncomingPhoneNumberDeleteException,
  IncomingPhoneNumberDeleteRequest
}
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber._
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

final class IncomingPhoneNumberDeleteTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientPhoneNumber].getSimpleName when {

    "incomingPhoneNumberDelete" should {
      "safely delete a single incoming phone number from the subaccount" in {
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber
        val req                               =
          IncomingPhoneNumberDeleteRequest(
            TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
            TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
          )

        val resultFut = instance.incomingPhoneNumberDelete.run(twilioConnectionSetting, req)

        resultFut.map(res => assert(res === Right(Done)))
      }

      "Return a left of a NotFound exception, if twilio return a 404 response" in {
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(404)
                .withBody(
                  """{"code": 20404, "message": "The requested resource /2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/IncomingPhoneNumbers/PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1.json was not found", "more_info": "https://www.twilio.com/docs/errors/20404", "status": 404}"""
                )
            )
        )

        val twilioConnectionSetting = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientPhoneNumber = TwilioClient.defaultImpl().phoneNumber
        val accountSid = TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        val numberSid  = TwilioPhoneNumber.Sid.unsafe("PNXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX1")
        val req        =
          IncomingPhoneNumberDeleteRequest(
            accountSid,
            numberSid
          )

        val resultFut = instance.incomingPhoneNumberDelete.run(twilioConnectionSetting, req)

        resultFut.map(res =>
          assert(
            res === Left(
              IncomingPhoneNumberDeleteException.PhoneNumberNotFound(accountSid, numberSid)
            )
          )
        )
      }
    }
  }
}
