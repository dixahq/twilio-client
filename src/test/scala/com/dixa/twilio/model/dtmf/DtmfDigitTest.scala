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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

final class DtmfDigitTest extends AnyWordSpec with TableDrivenPropertyChecks {

  "DtmfDigit" should {

    val numericalValues = Table(
      "numerical value",
      DtmfDigit.`0`,
      DtmfDigit.`1`,
      DtmfDigit.`2`,
      DtmfDigit.`3`,
      DtmfDigit.`4`,
      DtmfDigit.`5`,
      DtmfDigit.`6`,
      DtmfDigit.`7`,
      DtmfDigit.`8`,
      DtmfDigit.`9`
    )
    val nonNumericalValues = Table("non-numerical value", DtmfDigit.`#`, DtmfDigit.`*`)

    "provide a list of all possible numerical values" in {
      val result = DtmfDigit.allNumerical
      assert(result == numericalValues.toIndexedSeq)
    }

    "have each numerical value tell if it's numerical or not" in {
      forAll(numericalValues) { digit =>
        assert(digit.isNumerical)
      }
    }

    "have each non-numerical value tell if it's numerical or not" in {
      forAll(nonNumericalValues) { digit =>
        assert(!digit.isNumerical)
      }
    }
  }

}
