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

  private def toAlphanumericUnsafe(
      s: String
  ): Alphanumeric = {
    // contains only valid characters, is between 1 and 11 characters long,
    // contains at least one letter
    val alphanumericRegex = "^(?=.*[a-zA-Z])[a-zA-Z0-9 ]{1,11}$"
    // s.trim.isEmpty to make sure it isn't only space characters
    if (!s.matches(alphanumericRegex) || s.trim.isEmpty)
      throw new IllegalArgumentException("")
    else Alphanumeric(s)
  }

  def toMessageSender(from: String): MessageSender = {
    PhoneNumberE164(from) match {
      case Some(pn) => E164(pn)
      case None     => toAlphanumericUnsafe(from)
    }
  }
}
