package com.dixa.twilio.model.voice

import org.scalatest.wordspec.AnyWordSpec

class CallSidTest extends AnyWordSpec {

  s"Call.Sid" when {

    "constructed with apply method" should {
      // It looks like call sids are always CA and then 32 HEX characters. But the
      // official documentation says nothing else than:
      // "It is a 34 character string that starts with CA"
      // So lets enforce exactly that.
      // https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-

      "not accept empty strings as input" in {
        intercept[Call.Sid.ArgumentEmptyException] {
          Call.Sid("")
        }
      }

      "not accept input that does not start with CA" in {
        intercept[Call.Sid.ArgumentMissingCaPrefixException] {
          Call.Sid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }

      "not accept input that is not 31 chars long (Should always be CA plus 32 chars" in {
        intercept[Call.Sid.ArgumentLengthException] {
          Call.Sid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }

      "not accept input that is not 33 chars long (Should always be CA plus 32 chars" in {
        intercept[Call.Sid.ArgumentLengthException] {
          Call.Sid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
        }
      }
    }

    "constructed with safe method" should {

      "return right on valid input" in {
        val asString = "CAxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
        val resultE: Either[Call.Sid.CreationException, Call.Sid] = Call.Sid.safe(asString)
        val result = resultE.getOrElse(fail(s"Expected right side here but was: $resultE"))
        assert(result.toString === asString)
      }

      "return Left on empty strings as input" in {
        val resultE  = Call.Sid.safe("")
        val expected = Left(Call.Sid.ArgumentEmptyException())
        assert(resultE === expected)
      }

      "not accept input that does not start with CA" in {
        val asString = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE  = Call.Sid.safe(asString)
        val expected = Left(Call.Sid.ArgumentMissingCaPrefixException(asString))
        assert(resultE === expected)
      }

      "not accept input that is not 31 chars long (Should always be CA plus 32 chars" in {
        val asString = "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE  = Call.Sid.safe(asString)
        val expected = Left(Call.Sid.ArgumentLengthException(asString))
        assert(resultE === expected)
      }

      "not accept input that is not 33 chars long (Should always be CA plus 32 chars" in {
        val asString = "CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
        val resultE  = Call.Sid.safe(asString)
        val expected = Left(Call.Sid.ArgumentLengthException(asString))
        assert(resultE === expected)
      }
    }

    "not allow using the constructor, to bypass validation" in {
      assertDoesNotCompile("""new Call.Sid("NotValidInput")""")
    }
  }
}
