package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithApiName
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement._

import scala.collection.immutable

final case class PhoneNumberRegulatoryRequirement(
    addressRequirement: AddressRequirementType,
)

object PhoneNumberRegulatoryRequirement {
  sealed abstract class AddressRequirementType(val apiName: String)
      extends EnumWithApiName.EnumEntry

  object AddressRequirementType extends EnumWithApiName[AddressRequirementType] {
    override val values: immutable.IndexedSeq[AddressRequirementType] = findValues

    case object Any     extends AddressRequirementType("any")
    case object Local   extends AddressRequirementType("local")
    case object Foreign extends AddressRequirementType("foreign")
    case object None    extends AddressRequirementType("none")
  }

}
