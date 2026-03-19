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

import com.dixa.twilio.client.twilioClient.TwilioClientTest
import com.dixa.twilio.client.voice.ConferenceParticipantDeleteRequestExecutor.ConferenceParticipantDeleteRequest
import com.dixa.twilio.client.voice.TwilioClientVoice
import com.dixa.twilio.client.{TwilioClient, TwilioTestConstants}
import com.dixa.twilio.model.FUnit
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{Call, Conference}
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.scalatest.matchers.should.Matchers

final class ConferenceParticipantDeleteTest extends TwilioClientTest with Matchers {
  classOf[TwilioClientVoice].getSimpleName when {

    "conferenceParticipantDelete" should {
      "safely delete a single participant from a conference" in {
        wireMockServer.stubFor(
          WireMock
            .delete(
              WireMock.urlPathEqualTo(
                "/2010-04-01/Accounts/ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Conference/CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX/Participant/CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.json"
              )
            )
            .withBasicAuth("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", "testPassword")
            .willReturn(
              aResponse()
                .withStatus(204)
            )
        )

        val twilioConnectionSetting     = TwilioTestConstants.connSettings(wireMockServer.port())
        val instance: TwilioClientVoice = TwilioClient.defaultImpl().voice
        val req                         =
          ConferenceParticipantDeleteRequest.build(
            _.withAccountSid(TwilioAccount.Sid.unsafe("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .withConferenceSid(Conference.Sid.unsafe("CFXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .withCallSid(Call.Sid.unsafe("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"))
              .build()
          )

        val resultFut = instance.conferenceParticipantDelete.run(twilioConnectionSetting, req)

        resultFut.map(res => assert(res === Right(FUnit)))
      }
    }
  }
}
