package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

/** Base trait for the different kind of phone number entities in Twilio.
  */
trait TwilioPhoneNumber {

  // Common stuff should be moved down here, as we find the need for it.
}

object TwilioPhoneNumber {

  final case class Sid private[TwilioPhoneNumber] (override val toString: String)
      extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(Prefix("PN"), new Sid(_))
}
