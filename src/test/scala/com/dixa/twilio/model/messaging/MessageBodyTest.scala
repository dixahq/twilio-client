package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.messaging.MessageBody.isGSM7
import org.scalatest.wordspec.AnyWordSpec

class MessageBodyTest extends AnyWordSpec {

  classOf[MessageBody].getSimpleName when {

    "checking if text is in GSM-7 format" should {
      "return true for text that contains only GSM-7 characters" in {
        val input        = "Twilio is the telephony and sms provider and it has a console"
        val isGSM7Format = isGSM7(input)

        assert(isGSM7Format)
      }

      "return false for text that contains non GSM-7 characters" in {
        val input        = "Sėkmės ir gražios dienos"
        val isGSM7Format = isGSM7(input)

        assert(!isGSM7Format)
      }
    }
  }
}
