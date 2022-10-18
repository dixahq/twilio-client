package com.dixa.twilio.model.dtmf
import com.dixa.twilio.model.dtmf.DtmfDigit.DtmfDigitException
import com.dixa.twilio.model.dtmf.DtmfString.DtmfStringException
import org.scalatest.wordspec.AnyWordSpec

final class DtmfStringTest extends AnyWordSpec {

  DtmfString.getClass.getSimpleName when {

    "constructed from a string" should {

      "return a Right(result) when all safe variant is called with valid string" in {

        val in       = "#8w6"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfDigit.`w`, DtmfDigit.`6`)
        val result   = DtmfString.fromString(in)
        assert(result === Right(expected))
      }

      "return a result when all unsafe variant is called with valid string" in {

        val in       = "#8w6"
        val expected = DtmfString(DtmfDigit.`#`, DtmfDigit.`8`, DtmfDigit.`w`, DtmfDigit.`6`)
        val result   = DtmfString.fromStringUnsafe(in)
        assert(result === expected)
      }

      "return a Left if safe variant is provided an empty string" in {
        assert(DtmfString.fromString("") === Left(DtmfStringException.EmptyValue))
      }

      "throw exception if unsafe variant is provided an empty string" in {
        assertThrows[DtmfStringException.EmptyValue.type] {
          DtmfString.fromStringUnsafe("")
        }
      }

      "return a Left if safe variant is provided a string with invalid char in it" in {
        val in = "45w*#23w4I"
        assert(
          DtmfString.fromString(in) === Left(
            DtmfStringException.InvalidChar(DtmfDigitException.InvalidChar('I'))
          )
        )
      }

      "throw exception if unsafe variant is provided a string with invalid char in it" in {
        val in = "45w*#23w4I"
        assertThrows[DtmfStringException.InvalidChar] {
          DtmfString.fromStringUnsafe(in)
        }
      }
    }

    "should provide a map method" in {
      val in       = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected = DtmfString(DtmfDigit.`2`, DtmfDigit.`3`, DtmfDigit.`4`)
      val result = in.map { d =>
        val asInt   = d.twilioString.toInt
        val plusOne = asInt + 1
        DtmfDigit.fromCharUnsafe(plusOne.toString.head)
      }
      assert(result == expected)
    }

    "should provide a flatMap method" in {
      val in = DtmfString(DtmfDigit.`1`, DtmfDigit.`2`, DtmfDigit.`3`)
      val expected = DtmfString(
        DtmfDigit.`1`,
        DtmfDigit.`w`,
        DtmfDigit.`w`,
        DtmfDigit.`2`,
        DtmfDigit.`w`,
        DtmfDigit.`w`,
        DtmfDigit.`3`,
        DtmfDigit.`w`,
        DtmfDigit.`w`
      )
      val result = in.flatMap { d =>
        DtmfString(d, DtmfDigit.`w`, DtmfDigit.`w`)
      }
      assert(result == expected)
    }
  }
}
