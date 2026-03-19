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

package com.dixa.twilio.client.twilioClient.general

import com.dixa.twilio.CommonFixtures
import com.dixa.twilio.client.general.ApplicationDeleteRequestExecutor
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.general.Application
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

import scala.concurrent.Future

final class ApplicationDeleteTest extends TwilioClientTest {
  classOf[TwilioClientMessaging].getSimpleName when {

    "ask to delete an Application" should {
      "ask twilio to delete it" in {

        val request = ApplicationDeleteRequestExecutor.ApplicationDeleteRequest.build(
          _.withAccountSid(CommonFixtures.accountSid1)
            .withSid(Application.Sid.unsafe("APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX536"))
            .build()
        )

        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                s"/2010-04-01/Accounts/${CommonFixtures.accountSid1}/Applications/APXXXXXXXXXXXXXXXXXXXXXXXXXXXXX536.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val connSettings = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: ApplicationDeleteRequestExecutor =
          TwilioClient.defaultImpl().general.applicationDelete
        val resultFut: Future[
          Either[ApplicationDeleteRequestExecutor.ApplicationDeleteException, FUnit]
        ] = {
          instance.run(connSettings, request)
        }
        resultFut.map { result =>
          val succResult = result.getOrElse {
            val e = result.left.getOrElse(fail("No success or either, how can that happen :D"))
            fail("expected successfully result here", e)
          }
          assert(succResult === FUnit)
        }
      }
    }
  }
}
