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

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.noun.NumberNoun
import org.scalatest.wordspec.AnyWordSpec

final class DialVerbDestinationsTest extends AnyWordSpec {

  private def numberNouns(count: Int): List[NumberNoun] =
    List.tabulate(count) { index =>
      NumberNoun.build(
        _.withPhoneNumber(PhoneNumberE164.unsafe(s"+155500000${index + 10}")).build()
      )
    }

  classOf[DialVerb.Destinations].getSimpleName should {

    "return a Left if created without any noun" in {
      assert(DialVerb.Destinations.safe(Nil) == Left(DialVerb.Destinations.Err.CountOutOfRange(0)))
    }

    "return an instance if created with 1 noun" in {
      assert(DialVerb.Destinations.safe(numberNouns(1)).map(_.nouns) == Right(numberNouns(1)))
    }

    "return an instance if created with 10 nouns, the maximum Twilio allows in one Dial" in {
      assert(DialVerb.Destinations.safe(numberNouns(10)).map(_.nouns) == Right(numberNouns(10)))
    }

    "return a Left if created with 11 nouns" in {
      assert(
        DialVerb.Destinations.safe(numberNouns(11)) ==
          Left(DialVerb.Destinations.Err.CountOutOfRange(11))
      )
    }

    "throw an exception if created with 11 nouns via the unsafe method" in {
      intercept[DialVerb.Destinations.Err.CountOutOfRange](
        DialVerb.Destinations.unsafe(numberNouns(11))
      )
    }

    "not allow instances to be created without the count check" in {
      assertDoesNotCompile("DialVerb.Destinations(numberNouns(11))")
      assertDoesNotCompile("new DialVerb.Destinations(numberNouns(11))")
    }
  }
}
