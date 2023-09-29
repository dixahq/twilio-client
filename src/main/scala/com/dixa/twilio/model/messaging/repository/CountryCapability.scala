package com.dixa.twilio.model.messaging.repository

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.messaging.repository.CountryCapability.AlphanumericSenderSupport
import com.dixa.twilio.model.phonenumber.CountryCallingCode

import scala.collection.immutable

/** Represents the SMS capabilities of a country.
  * @param countryCallingCode
  *   country code of the country.
  * @param name
  *   Name of country. This is only for debugging and overview purposes, and is not meant as a
  *   legally correct name
  * @param alphanumericSenderSupport
  *   Represents the support for Alphanumeric sender id (using some text as sender)
  */
final case class CountryCapability(
    countryCallingCode: CountryCallingCode,
    name: String,
    alphanumericSenderSupport: AlphanumericSenderSupport
)

object CountryCapability {

  sealed abstract class AlphanumericSenderSupport(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object AlphanumericSenderSupport extends EnumWithTwilioString[AlphanumericSenderSupport] {
    override val values: immutable.IndexedSeq[AlphanumericSenderSupport] = findValues

    case object Yes extends AlphanumericSenderSupport("yes")

    case object PreRegistrationRequired extends AlphanumericSenderSupport("preRegistrationRequired")

    case object No extends AlphanumericSenderSupport("no")
  }
}
