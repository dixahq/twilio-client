package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.messaging.MessageRecipient

import scala.util.Try

sealed trait PhoneNumberE164 extends MessageRecipient {

  def asString: String
  override final val toString = asString
}

object PhoneNumberE164 {

  def unsafe(asString: String): PhoneNumberE164 = {
    require(verifyPattern.matcher(asString).matches(), s"$toString is not in E.164 format")
    DefaultImpl(asString)
  }

  def apply(asString: String): Option[PhoneNumberE164] = Try {
    unsafe(asString)
  }.toOption

  private val verifyPattern = """^\+[1-9]\d{1,14}$""".r.pattern

  private final case class DefaultImpl(asString: String) extends PhoneNumberE164
}
