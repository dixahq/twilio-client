package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement._
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

final case class PhoneNumberRegulatoryRequirement(
    addressRequirement: AddressRequirementType,
)

object PhoneNumberRegulatoryRequirement {
  sealed abstract class AddressRequirementType(val apiName: String) extends EnumEntry

  object AddressRequirementType extends Enum[AddressRequirementType] {
    override val values: immutable.IndexedSeq[AddressRequirementType] = findValues

    case object Any     extends AddressRequirementType("any")
    case object Local   extends AddressRequirementType("local")
    case object Foreign extends AddressRequirementType("foreign")
    case object None    extends AddressRequirementType("none")

    def fromApiNameCaseInsensitive(s: String): AddressRequirementType = values
      .find(_.apiName.toLowerCase === s.toLowerCase)
      .getOrElse(
        throw new IllegalArgumentException(
          s"$s is not a valid Twilio AddressRequirementType. Valid values are: $values"
        )
      )
  }

}
