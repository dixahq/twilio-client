package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.TwilioStringValue

trait MessageAddressee extends TwilioStringValue {
  def toMessageAddressee: String = toString
}
