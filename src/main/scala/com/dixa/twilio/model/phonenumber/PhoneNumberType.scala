package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class PhoneNumberType(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry

object PhoneNumberType extends EnumWithTwilioString[PhoneNumberType] {
  override val values: immutable.IndexedSeq[PhoneNumberType] = findValues

  case object Local     extends PhoneNumberType("local")
  case object National  extends PhoneNumberType("national")
  case object Mobile    extends PhoneNumberType("mobile")
  case object TollFree  extends PhoneNumberType("tollfree")
  case object ShortCode extends PhoneNumberType("shortcode")
}
