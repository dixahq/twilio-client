package com.dixa.twilio.model.dtmf

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.prop.TableDrivenPropertyChecks

final class DtmfDigitTest extends AnyWordSpec with TableDrivenPropertyChecks {

  "DtmfDigit" should {

    val numericalValues = Table("numerical value", DtmfDigit.`0`, DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`, DtmfDigit.`4`, DtmfDigit.`5`, DtmfDigit.`6`, DtmfDigit.`7`, DtmfDigit.`8`, DtmfDigit.`9`)
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
