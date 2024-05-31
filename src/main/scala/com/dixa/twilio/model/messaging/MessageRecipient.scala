package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.TwilioStringValue
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.whatsapp.WhatsappNumber

abstract class MessageRecipient private[model] extends TwilioStringValue {

  def toMessageRecipient: String = toString
}

object MessageRecipient {
  def fromString(string: String): Option[MessageRecipient] = {
    PhoneNumberE164(string)
      .orElse(WhatsappNumber(string))
  }

  def fromStringUnsafe(string: String): MessageRecipient = {
    fromString(string).getOrElse(
      throw new IllegalArgumentException(
        "Recipient couldn't be parsed neither into Whatsapp nor into E.164 phone number"
      )
    )
  }
}
