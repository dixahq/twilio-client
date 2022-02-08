package com.dixa.twilio.client.model.phonenumber

import com.dixa.twilio.client.model.phonenumber.PhoneNumberRegulatoryRequirement._
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

case class PhoneNumberRegulatoryRequirement(
    addressRequirement: AddressRequirementType,
)

object PhoneNumberRegulatoryRequirement {
  sealed abstract class AddressRequirementType(private[client] val asString: String)
      extends EnumEntry

  object AddressRequirementType extends Enum[AddressRequirementType] {
    override val values: immutable.IndexedSeq[AddressRequirementType] = findValues

    case object Any     extends AddressRequirementType("any")
    case object Local   extends AddressRequirementType("local")
    case object Foreign extends AddressRequirementType("foreign")
    case object None    extends AddressRequirementType("none")

    private[client] def fromStringCaseInsensitive(s: String): AddressRequirementType = findValues
      .find(_.asString.toLowerCase === s.toLowerCase)
      .getOrElse(
        throw new IllegalArgumentException(s"$s is not a valid Twilio AddressRequirementType.")
      )
  }

}
