package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement._

import scala.collection.immutable

final case class PhoneNumberRegulatoryRequirement(
    addressRequirement: AddressRequirementType,
)

object PhoneNumberRegulatoryRequirement {
  sealed abstract class AddressRequirementType(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object AddressRequirementType extends EnumWithTwilioString[AddressRequirementType] {
    override val values: immutable.IndexedSeq[AddressRequirementType] = findValues

    case object Any     extends AddressRequirementType("any")
    case object Local   extends AddressRequirementType("local")
    case object Foreign extends AddressRequirementType("foreign")
    case object None    extends AddressRequirementType("none")
  }

}
