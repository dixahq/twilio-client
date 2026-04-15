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

import com.dixa.twilio.client.TwilioTestConstants
import com.dixa.twilio.client.phonenumber.IncomingNumbersReadRequestExecutor.IncomingNumbersReadRequest
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber
import org.scalatest.wordspec.AnyWordSpec

final class IncomingNumbersReadRequestBuilderTest extends AnyWordSpec {

  classOf[IncomingNumbersReadRequest].getSimpleName when {

    "building a request" should {

      "succeed when only the required accountSid is set" in {
        IncomingNumbersReadRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .build()
        )
      }

      "succeed when accountSid and the optional filter are set" in {
        IncomingNumbersReadRequest.build(
          _.withAccountSid(TwilioTestConstants.accountSid)
            .withFilter(TwilioIncomingPhoneNumber.PhoneNumberFilter("+45"))
            .build()
        )
      }

      "not compile when accountSid is missing" in {
        assertDoesNotCompile("IncomingNumbersReadRequest.Builder.empty.build()")
      }

      "not compile when accountSid is missing even with filter set" in {
        assertDoesNotCompile(
          """IncomingNumbersReadRequest.Builder.empty
               .withFilter(TwilioIncomingPhoneNumber.PhoneNumberFilter("+45"))
               .build()"""
        )
      }
    }
  }
}
