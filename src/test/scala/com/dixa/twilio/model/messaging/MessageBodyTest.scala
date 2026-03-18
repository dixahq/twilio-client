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

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.messaging.MessageBody.isGSM7
import org.scalatest.wordspec.AnyWordSpec

class MessageBodyTest extends AnyWordSpec {

  classOf[MessageBody].getSimpleName when {

    "checking if text is in GSM-7 format" should {
      "return true for text that contains only GSM-7 characters" in {
        val input        = "Twilio is the telephony and sms provider and it has a developer console"
        val isGSM7Format = isGSM7(input)

        assert(isGSM7Format)
      }

      "return true for Line Feed \n" in {
        val input        = "Twilio is the telephony and sms provider\n"
        val isGSM7Format = isGSM7(input)

        assert(isGSM7Format)
      }

      "return true for Carriage Return \r" in {
        val input        = "Twilio is the telephony and sms provider\r"
        val isGSM7Format = isGSM7(input)

        assert(isGSM7Format)
      }

      "return false for text that contains non GSM-7 characters" in {
        val input        = "Sėkmės ir gražios dienos"
        val isGSM7Format = isGSM7(input)

        assert(!isGSM7Format)
      }
    }
  }
}
