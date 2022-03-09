package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.phonenumber.PhoneNumberE164

// There is also WirelessSIM, not included
sealed abstract class MessageSender {
  def asString: String
}

object MessageSender {
  final case class E164(phoneNumber: PhoneNumberE164) extends MessageSender {
    override def asString: String = phoneNumber.asString
  }
  final case class Alphanumeric(override val asString: String) extends MessageSender
}
