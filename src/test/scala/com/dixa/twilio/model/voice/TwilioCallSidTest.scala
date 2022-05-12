package com.dixa.twilio.model.voice

import org.scalatest.wordspec.AnyWordSpec

class TwilioCallSidTest extends AnyWordSpec {

  s"${classOf[TwilioCallSid].getSimpleName}" should {

    // It looks like call sids are always CA and then 32 HEX characters. But the
    // official documentation says nothing else than:
    // "It is a 34 character string that starts with CA"
    // So lets enforce exactly that.
    // https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-

    "not accept empty strings as input" in {
      intercept[IllegalArgumentException] {
        TwilioCallSid("")
      }
    }

    "not accept input that does not start with CA" in {
      intercept[IllegalArgumentException] {
        TwilioCallSid("ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      }
    }

    "not accept input that is not 31 chars long (Should always be CA plus 32 chars" in {
      intercept[IllegalArgumentException] {
        TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      }
    }

    "not accept input that is not 33 chars long (Should always be CA plus 32 chars" in {
      intercept[IllegalArgumentException] {
        TwilioCallSid("CAXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
      }
    }
  }
}
