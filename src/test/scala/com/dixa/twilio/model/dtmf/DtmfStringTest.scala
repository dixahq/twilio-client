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

package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.dtmf.DtmfDigit.DtmfDigitException
import com.dixa.twilio.model.dtmf.DtmfString.{DtmfStringElement, DtmfStringException}
import org.scalatest.wordspec.AnyWordSpec
import scala.collection.immutable

final class DtmfStringTest extends AnyWordSpec {

  classOf[DtmfString].getSimpleName when {

    "constructed from a string" should {

      "return a Right(result) when fromStringIncludeWaits safe variant is called" in {

        val in       = "#8w6"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfString.`w`, DtmfDigit.`6`)
        val result   = DtmfString.fromStringIncludeWaits(in)
        assert(result === Right(expected))
      }

      "return a result when fromStringIncludeWaitsUnsafe is called" in {

        val in       = "#8w6"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfString.`w`, DtmfDigit.`6`)
        val result   = DtmfString.fromStringIncludeWaitsUnsafe(in)
        assert(result === expected)
      }

      "return a Left if fromStringIncludeWaits safe variant is provided an empty string" in {
        assert(DtmfString.fromStringIncludeWaits("") === Left(DtmfStringException.EmptyValue))
      }

      "throw exception if fromStringIncludeWaitsUnsafe is provided an empty string" in {
        assertThrows[DtmfStringException.EmptyValue.type] {
          DtmfString.fromStringIncludeWaitsUnsafe("")
        }
      }

      "return a Left if fromStringIncludeWaits safe variant is provided a string with invalid char in it" in {
        val in = "45w*#23w4I"
        assert(
          DtmfString.fromStringIncludeWaits(in) === Left(
            DtmfStringException.InvalidChar(DtmfDigitException.InvalidChar('I'))
          )
        )
      }

      "throw exception if fromStringIncludeWaitsUnsafe is provided a string with invalid char in it" in {
        val in = "45w*#23w4I"
        assertThrows[DtmfStringException.InvalidChar] {
          DtmfString.fromStringIncludeWaitsUnsafe(in)
        }
      }

      "return a Right(result) when fromStringOnlyDtmfDigits safe variant is called" in {

        val in       = "#86"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfDigit.`6`)
        val result   = DtmfString.fromStringOnlyDtmfDigits(in)
        assert(result === Right(expected))
      }

      "return a result when fromStringOnlyDtmfDigitsUnsafe is called" in {

        val in       = "#86"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfDigit.`6`)
        val result: DtmfString.OnlyDtmfDigits = DtmfString.fromStringOnlyDtmfDigitsUnsafe(in)
        assert(result === expected)
      }

      "return a Left if fromStringOnlyDtmfDigits safe variant is provided an empty string" in {
        assert(DtmfString.fromStringOnlyDtmfDigits("") === Left(DtmfStringException.EmptyValue))
      }

      "throw exception if fromStringOnlyDtmfDigitsUnsafe is provided an empty string" in {
        assertThrows[DtmfStringException.EmptyValue.type] {
          DtmfString.fromStringOnlyDtmfDigitsUnsafe("")
        }
      }

      "return a Left if fromStringOnlyDtmfDigits safe variant is provided a string with invalid char in it" in {
        val in = "45*#234I"
        assert(
          DtmfString.fromStringOnlyDtmfDigits(in) === Left(
            DtmfStringException.InvalidChar(DtmfDigitException.InvalidChar('I'))
          )
        )
      }

      "throw exception if fromStringOnlyDtmfDigitsUnsafe is provided a string with invalid char in it" in {
        val in = "45*#234I"
        assertThrows[DtmfStringException.InvalidChar] {
          DtmfString.fromStringOnlyDtmfDigitsUnsafe(in)
        }
      }

      "return a Left if fromStringOnlyDtmfDigits safe variant is provided a string with wait char w" in {
        val in = "45w*#234I"
        assert(
          DtmfString.fromStringOnlyDtmfDigits(in) === Left(
            DtmfStringException.InvalidChar(DtmfDigitException.InvalidChar('w'))
          )
        )
      }

      "throw exception if fromStringOnlyDtmfDigitsUnsafe is provided a string with wait char w" in {
        val in = "45w*#234I"
        assertThrows[DtmfStringException.InvalidChar] {
          DtmfString.fromStringOnlyDtmfDigitsUnsafe(in)
        }
      }
    }

    "constructed with varargs" should {
      "return a OnlyDtmfDigits instance when only constructed from DtmfDigit instances" in {

        val result: DtmfString.OnlyDtmfDigits =
          DtmfString(DtmfDigit.`#`, DtmfDigit.`3`, DtmfDigit.`*`, DtmfDigit.`5`)
        assert(result.twilioString === "#3*5")
      }

      "return a IncludeWaits instance when only constructed with included waits beside DtmfDigit instances" in {
        val result: DtmfString.IncludeWaits =
          DtmfString(DtmfDigit.`#`, DtmfDigit.`3`, DtmfString.w, DtmfDigit.`5`)
        assert(result.twilioString === "#3w5")
      }
    }

    "constructed from a Seq" should {
      "return a OnlyDtmfDigits instance when only constructed from DtmfDigit instances" in {

        val result: DtmfString.OnlyDtmfDigits =
          DtmfString.fromSeq(DtmfDigit.`#`, List(DtmfDigit.`3`, DtmfDigit.`*`, DtmfDigit.`5`))
        assert(result.twilioString === "#3*5")
      }

      "return a IncludeWaits instance when only constructed with included waits beside DtmfDigit instances" in {
        val result: DtmfString.IncludeWaits =
          DtmfString.fromSeq(DtmfDigit.`#`, List(DtmfDigit.`3`, DtmfString.w, DtmfDigit.`5`))
        assert(result.twilioString === "#3w5")
      }
    }

    "allow map of an OnlyDtmfDigits instance into a IncludeWait instance" in {
      val in: DtmfString.OnlyDtmfDigits = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected                      = DtmfString(DtmfString.`w`, DtmfDigit.`3`, DtmfDigit.`4`)
      val result                        = in.map { d =>
        if (d == DtmfDigit.`1`) DtmfString.w
        else {
          val asInt   = d.twilioString.toInt
          val plusOne = asInt + 1
          val digit   = DtmfDigit.fromCharUnsafe(plusOne.toString.head)
          DtmfStringElement.fromDtmfDigit(digit)
        }
      }
      assert(result == expected)
    }

    "in the onlyDtmfDigits instance, provide a way to just get a Seq[DtmfDigit] out of it" in {
      val in: DtmfString.OnlyDtmfDigits    = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val result: immutable.Seq[DtmfDigit] = in.asSeqDtmfDigit
      val expected                         = List(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      assert(result == expected)
    }

    "allow map of an OnlyDtmfDigits instance into another OnlyDtmfDigits instance" in {
      val in: DtmfString.OnlyDtmfDigits = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected: DtmfString.OnlyDtmfDigits =
        DtmfString(DtmfDigit.`2`, DtmfDigit.`3`, DtmfDigit.`4`)
      val result = in.map { d =>
        val asInt   = d.twilioString.toInt
        val plusOne = asInt + 1
        DtmfDigit.fromCharUnsafe(plusOne.toString.head)
      }
      assert(result == expected)
    }

    "allow map of an IncludeWaits instance into another IncludeWaits instance" in {
      val in: DtmfString.IncludeWaits = DtmfString(DtmfString.w, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected                    =
        DtmfString(DtmfString.w, DtmfDigit.`3`, DtmfDigit.`4`)
      val result: DtmfString.IncludeWaits = in.map {
        case DtmfString.DtmfStringElement.WaitElement         => DtmfString.w
        case DtmfString.DtmfStringElement.DtmfDigitElement(d) =>
          val asInt   = d.twilioString.toInt
          val plusOne = asInt + 1
          DtmfStringElement.fromDtmfDigit(DtmfDigit.fromCharUnsafe(plusOne.toString.head))
      }
      assert(result == expected)
    }

    "allow map of an IncludeWaits instance into another OnlyDtmfDigits instance" in {
      val in: DtmfString.IncludeWaits = DtmfString(DtmfString.w, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected: DtmfString.OnlyDtmfDigits =
        DtmfString(DtmfDigit.`4`, DtmfDigit.`4`, DtmfDigit.`4`)
      val result: DtmfString.OnlyDtmfDigits = in.map(_ => DtmfDigit.`4`)
      assert(result == expected)
    }

    "allow flatmap of an OnlyDtmfDigits instance into a IncludeWait instance" in {
      val in: DtmfString.OnlyDtmfDigits = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected: DtmfString.IncludeWaits = DtmfString(
        DtmfDigit.`1`,
        DtmfString.`w`,
        DtmfString.`w`,
        DtmfDigit.`2`,
        DtmfString.`w`,
        DtmfString.`w`,
        DtmfDigit.`3`,
        DtmfString.`w`,
        DtmfString.`w`
      )
      val result: DtmfString.IncludeWaits = in.flatMap { d =>
        DtmfString(d, DtmfString.`w`, DtmfString.`w`)
      }
      assert(result == expected)
    }

    "allow flatmap of an OnlyDtmfDigits instance into another OnlyDtmfDigits instance" in {
      val in: DtmfString.OnlyDtmfDigits = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected: DtmfString.OnlyDtmfDigits = DtmfString(
        DtmfDigit.`1`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`2`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`3`,
        DtmfDigit.`#`,
        DtmfDigit.`#`
      )
      val result: DtmfString.OnlyDtmfDigits = in.flatMap { d =>
        DtmfString(d, DtmfDigit.`#`, DtmfDigit.`#`)
      }
      assert(result == expected)
    }

    "allow flatmap of an IncludeWaits instance into another IncludeWaits instance" in {
      val in: DtmfString.IncludeWaits = DtmfString(DtmfString.w, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected                    = DtmfString(
        DtmfString.w,
        DtmfString.w,
        DtmfString.w,
        DtmfDigit.`2`,
        DtmfString.w,
        DtmfString.w,
        DtmfDigit.`3`,
        DtmfString.w,
        DtmfString.w
      )
      val result: DtmfString.IncludeWaits = in.flatMap { d =>
        DtmfString(d, DtmfString.w, DtmfString.w)
      }
      assert(result == expected)
    }

    "allow flatmap of an IncludeWaits instance into another OnlyDtmfDigits instance" in {
      val in: DtmfString.IncludeWaits = DtmfString(DtmfString.w, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected: DtmfString.OnlyDtmfDigits = DtmfString(
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`,
        DtmfDigit.`#`
      )
      val result: DtmfString.OnlyDtmfDigits = in.flatMap { _ =>
        DtmfString(DtmfDigit.`#`, DtmfDigit.`#`, DtmfDigit.`#`)
      }
      assert(result == expected)
    }
  }
}
