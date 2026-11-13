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

package com.dixa.twilio.model.twiml.verb

import org.scalatest.wordspec.AnyWordSpec

final class DialVerbTimeoutTest extends AnyWordSpec {

  classOf[DialVerb.Timeout].getSimpleName should {

    "return a Left if created with 4 seconds, below Twilio's minimum of 5" in {
      assert(DialVerb.Timeout.safe(4) == Left(DialVerb.Timeout.Err.NumberOutOfRange(4)))
    }

    "return an instance if created with 5 seconds" in {
      assert(DialVerb.Timeout.safe(5).map(_.seconds) == Right(5))
    }

    "return an instance if created with 600 seconds" in {
      assert(DialVerb.Timeout.safe(600).map(_.seconds) == Right(600))
    }

    "return a Left for seconds that do not fit an Int" in {
      assert(
        DialVerb.Timeout.safe(Int.MaxValue.toLong + 1) ==
          Left(DialVerb.Timeout.Err.NumberOutOfRange(2147483648L))
      )
    }

    "say in the message what the limits are" in {
      assert(
        DialVerb.Timeout.safe(4).swap.toOption.get.getMessage ==
          "4 is not between 5 and 600 seconds as required for the timeout of a Dial."
      )
    }

    "return a Left if created with 601 seconds, above Twilio's maximum of 600" in {
      assert(DialVerb.Timeout.safe(601) == Left(DialVerb.Timeout.Err.NumberOutOfRange(601)))
    }

    "throw an exception if created with 601 seconds via the unsafe method" in {
      intercept[DialVerb.Timeout.Err.NumberOutOfRange](DialVerb.Timeout.unsafe(601))
    }

    "not allow instances to be created without the range check" in {
      assertDoesNotCompile("DialVerb.Timeout(900)")
      assertDoesNotCompile("new DialVerb.Timeout(900)")
      assertDoesNotCompile("DialVerb.Timeout.unsafe(30).copy(seconds = 900)")
    }
  }
}
