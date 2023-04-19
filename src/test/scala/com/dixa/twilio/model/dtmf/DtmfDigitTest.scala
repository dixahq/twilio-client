package com.dixa.twilio.model.dtmf

import org.scalatest.wordspec.AnyWordSpec
import scala.collection.immutable

final class DtmfDigitTest extends AnyWordSpec {

  classOf[DtmfDigit].getSimpleName should {

    "provide a list of all possible numerical values" in {
      val result = DtmfDigit.allNumerical
      val expected: immutable.IndexedSeq[DtmfDigit] = Vector(
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
      assert(result == expected)
    }

    "have each value tell if it's numerical or not" in {
      assert(!DtmfDigit.`#`.isNumerical)
      assert(!DtmfDigit.`*`.isNumerical)
      assert(DtmfDigit.`0`.isNumerical)
      assert(DtmfDigit.`1`.isNumerical)
      assert(DtmfDigit.`2`.isNumerical)
      assert(DtmfDigit.`3`.isNumerical)
      assert(DtmfDigit.`4`.isNumerical)
      assert(DtmfDigit.`5`.isNumerical)
      assert(DtmfDigit.`6`.isNumerical)
      assert(DtmfDigit.`7`.isNumerical)
      assert(DtmfDigit.`8`.isNumerical)
      assert(DtmfDigit.`9`.isNumerical)
    }
  }

}
