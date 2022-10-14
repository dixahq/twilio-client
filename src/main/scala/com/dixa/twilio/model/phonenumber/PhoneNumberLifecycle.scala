package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class PhoneNumberLifecycle(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry

object PhoneNumberLifecycle extends EnumWithTwilioString[PhoneNumberLifecycle] {
  override val values: immutable.IndexedSeq[PhoneNumberLifecycle] = findValues

  case object Beta               extends PhoneNumberLifecycle("beta")
  case object DeveloperPreview   extends PhoneNumberLifecycle("developer-preview")
  case object GenerallyAvailable extends PhoneNumberLifecycle("generally-available")
  case object Exhausted          extends PhoneNumberLifecycle("exhausted")
}
