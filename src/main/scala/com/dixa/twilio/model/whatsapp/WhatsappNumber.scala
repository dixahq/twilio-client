package com.dixa.twilio.model.whatsapp

import com.dixa.twilio.model.messaging.MessageRecipient
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import scala.util.Try

sealed trait WhatsappNumber extends MessageRecipient {
  def number: PhoneNumberE164

  private def asString: String = WhatsappNumber.Prefix + number.toString

  override final val toString = asString
}

object WhatsappNumber {
  private val Prefix = "whatsapp:"

  def unsafe(asString: String): WhatsappNumber = {
    require(
      asString.startsWith(Prefix),
      s"$toString is not a valid whatsapp number, must start with '$Prefix'"
    )
    val number = PhoneNumberE164.unsafe(asString.drop(Prefix.length))
    DefaultImpl(number)
  }

  def apply(asString: String): Option[WhatsappNumber] = Try {
    unsafe(asString)
  }.toOption

  def fromPhoneNumberE164(number: PhoneNumberE164): WhatsappNumber = DefaultImpl(number)

  private final case class DefaultImpl(number: PhoneNumberE164) extends WhatsappNumber
}
