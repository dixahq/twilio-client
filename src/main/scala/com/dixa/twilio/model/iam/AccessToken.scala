package com.dixa.twilio.model.iam

import com.dixa.twilio.model.TwilioStringValue

final case class AccessToken(token: String) extends TwilioStringValue {
  override def twilioString: String = token
}
