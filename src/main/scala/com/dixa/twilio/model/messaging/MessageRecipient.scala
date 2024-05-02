package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.TwilioStringValue

abstract class MessageRecipient private[model] extends TwilioStringValue {

  def toMessageRecipient: String = toString
}
