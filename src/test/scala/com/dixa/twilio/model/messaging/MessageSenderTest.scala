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

import com.dixa.twilio.model.messaging.MessageSender.Alphanumeric
import org.scalatest.wordspec.AnyWordSpec

class MessageSenderTest extends AnyWordSpec {

  classOf[MessageSender.Alphanumeric].getSimpleName when {

    "created from a valid alphanumeric sender id" should {
      "succeed 1" in {
        val alphanumericSender = "Twilio"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed 2" in {
        val alphanumericSender = "Cafe Bar 33"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed 3" in {
        val alphanumericSender = "130 cakes"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed 4" in {
        val alphanumericSender = "1337 5p34k"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed 5" in {
        val alphanumericSender = "ma23am"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed with one letter" in {
        val alphanumericSender = "D12345"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == alphanumericSender)
      }

      "succeed in trimming away leading or trailing whitespaces" in {
        val alphanumericSender = "  Candy  "
        val result             = Alphanumeric
          .fromString(alphanumericSender)
          .getOrElse(
            fail("This SmsSender is supposed to be a valid SmsSender")
          )

        assert(result.asString == "Candy")
      }
    }

    "created from invalid alphanumeric sender id" should {
      "fail when provided alphanumeric sender is longer than 11 characters" in {
        val alphanumericSender = "Ring ring it is Twilio"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
        assert(result.isLeft)
      }

      "fail when provided alphanumeric sender includes invalid characters" in {
        val alphanumericSender = "*Twilio*"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
        assert(result.isLeft)
      }

      "fail when provided alphanumeric sender is only space characters" in {
        val alphanumericSender = "   "
        val result             = Alphanumeric
          .fromString(alphanumericSender)
        assert(result.isLeft)
      }

      "fail when provided alphanumeric sender consists only of numbers and no letters" in {
        val alphanumericSender = "56789"
        val result             = Alphanumeric
          .fromString(alphanumericSender)
        assert(result.isLeft)
      }
    }
  }
}
