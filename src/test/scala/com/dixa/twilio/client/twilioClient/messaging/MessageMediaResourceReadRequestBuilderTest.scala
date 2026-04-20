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

package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest
import com.dixa.twilio.model.messaging.Message
import org.scalatest.wordspec.AnyWordSpec

final class MessageMediaResourceReadRequestBuilderTest extends AnyWordSpec {

  private val messageSid = Message.Sid.unsafe("SM9c8a124127702f0c7084b373cb06157a")

  classOf[MessageMediaResourceReadRequest].getSimpleName when {

    "building a request" should {

      "succeed when all required fields are set" in {
        MessageMediaResourceReadRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .withMessageSid(messageSid)
            .build()
        )
      }

      "not compile when accountSid is missing" in {
        assertDoesNotCompile(
          """MessageMediaResourceReadRequest.Builder.empty
               .withMessageSid(Message.Sid.unsafe("SM9c8a124127702f0c7084b373cb06157a"))
               .build()"""
        )
      }

      "not compile when messageSid is missing" in {
        assertDoesNotCompile(
          """MessageMediaResourceReadRequest.Builder.empty
               .withAccountSid(TwilioTestConstants.accountSid)
               .build()"""
        )
      }

      "not compile when no fields are set" in {
        assertDoesNotCompile("MessageMediaResourceReadRequest.Builder.empty.build()")
      }
    }
  }
}
