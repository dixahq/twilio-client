package com.dixa.twilio.client.model.phonenumber

trait PhoneNumberE164 {

  def asString: String

  override final val toString = asString
}

object PhoneNumberE164 {

  def apply(asString: String): PhoneNumberE164 = {
    require(verifyPattern.matcher(asString).matches(), s"$toString is not in E.164 format")
    DefaultImpl(asString)
  }

  /** Used internally when we see no need for checking the format.
    *
    * Like when we receive it from Twilio and knows that is is in E164 format.
    */
  private[client] def unchecked(asString: String): PhoneNumberE164 = DefaultImpl(asString)

  private val verifyPattern = """^\+[1-9]\d{1,14}$""".r.pattern

  private final case class DefaultImpl(asString: String) extends PhoneNumberE164
}
