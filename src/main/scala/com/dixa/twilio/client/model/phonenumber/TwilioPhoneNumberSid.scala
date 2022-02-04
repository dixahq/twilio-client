package com.dixa.twilio.client.model.phonenumber

/** Represent a Sid of a PhoneNumber in Twilio.
  *
  * Sid seems to have different levels, depending on there state in Twilio, so this is therefore a
  * sealed trait, with different implementations to represent that hierarchy.
  */
sealed trait TwilioPhoneNumberSid {

  def asString: String

  final override val toString = asString
}

object TwilioPhoneNumberSid {

  def apply(asString: String): TwilioPhoneNumberSid = DefaultImpl(asString)

  sealed trait IncomingPhoneNumberSid extends TwilioPhoneNumberSid

  object IncomingPhoneNumberSid {
    def apply(asString: String): IncomingPhoneNumberSid = DefaultIncomingImpl(asString)
  }

  private final case class DefaultImpl(asString: String)         extends TwilioPhoneNumberSid
  private final case class DefaultIncomingImpl(asString: String) extends IncomingPhoneNumberSid

}
