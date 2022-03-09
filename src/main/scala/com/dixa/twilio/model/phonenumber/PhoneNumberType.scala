package com.dixa.twilio.model.phonenumber

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

sealed abstract class PhoneNumberType(val apiName: String) extends EnumEntry

object PhoneNumberType extends Enum[PhoneNumberType] {
  override val values: immutable.IndexedSeq[PhoneNumberType] = findValues

  case object Local     extends PhoneNumberType("local")
  case object National  extends PhoneNumberType("national")
  case object Mobile    extends PhoneNumberType("mobile")
  case object TollFree  extends PhoneNumberType("tollfree")
  case object ShortCode extends PhoneNumberType("shortcode")

  def fromApiNameCaseInsensitive(s: String): PhoneNumberType = values
    .find(_.apiName.toLowerCase === s.toLowerCase)
    .getOrElse(
      throw new IllegalArgumentException(
        s"$s is not a valid Twilio phone number type. Valid values are: $values"
      )
    )
}
