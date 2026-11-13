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

final class DialVerbNounsTest extends AnyWordSpec {

  private def numberNouns(count: Int): List[NumberNoun] =
    List.tabulate(count) { index =>
      NumberNoun.build(
        _.withPhoneNumber(PhoneNumberE164.unsafe(s"+155500000${index + 10}")).build()
      )
    }

  private val elevenNouns = numberNouns(11)

  classOf[DialVerb.Nouns].getSimpleName should {

    "build an instance from 1 noun with apply" in {
      val nouns = numberNouns(1)
      assert(DialVerb.Nouns(nouns(0)).nouns == nouns)
    }

    "build an instance from 10 nouns with apply, in the order given" in {
      val nouns  = numberNouns(10)
      val result = DialVerb.Nouns(
        nouns(0),
        nouns(1),
        nouns(2),
        nouns(3),
        nouns(4),
        nouns(5),
        nouns(6),
        nouns(7),
        nouns(8),
        nouns(9)
      )
      assert(result.nouns == nouns)
    }

    "not compile apply with 11 nouns, one more than Twilio allows in one Dial" in {
      assertTypeError(
        """DialVerb.Nouns(
          |  elevenNouns(0),
          |  elevenNouns(1),
          |  elevenNouns(2),
          |  elevenNouns(3),
          |  elevenNouns(4),
          |  elevenNouns(5),
          |  elevenNouns(6),
          |  elevenNouns(7),
          |  elevenNouns(8),
          |  elevenNouns(9),
          |  elevenNouns(10)
          |)""".stripMargin
      )
    }

    "return a Left if created without any noun" in {
      assert(DialVerb.Nouns.fromSeq(Nil) == Left(DialVerb.Nouns.Err.CountOutOfRange(0)))
    }

    "return an instance if created with 1 noun" in {
      assert(DialVerb.Nouns.fromSeq(numberNouns(1)).map(_.nouns) == Right(numberNouns(1)))
    }

    "return an instance if created with 10 nouns, the maximum Twilio allows in one Dial" in {
      assert(DialVerb.Nouns.fromSeq(numberNouns(10)).map(_.nouns) == Right(numberNouns(10)))
    }

    "return a Left if created with 11 nouns" in {
      assert(DialVerb.Nouns.fromSeq(elevenNouns) == Left(DialVerb.Nouns.Err.CountOutOfRange(11)))
    }

    "say in the message what the limits are" in {
      assert(
        DialVerb.Nouns.fromSeq(elevenNouns).swap.toOption.get.getMessage ==
          "A Dial verb needs 1 to 10 nouns, not 11."
      )
    }

    "throw an exception if created with 11 nouns via fromSeqUnsafe" in {
      intercept[DialVerb.Nouns.Err.CountOutOfRange](DialVerb.Nouns.fromSeqUnsafe(elevenNouns))
    }

    "not allow instances to be created from a list without the count check" in {
      assertDoesNotCompile("DialVerb.Nouns(elevenNouns)")
      assertDoesNotCompile("new DialVerb.Nouns(elevenNouns)")
    }
  }
}
