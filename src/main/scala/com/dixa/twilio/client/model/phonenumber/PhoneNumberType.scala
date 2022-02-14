package com.dixa.twilio.client.model.phonenumber

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

sealed abstract class PhoneNumberType(private[client] val asString: String) extends EnumEntry

object PhoneNumberType extends Enum[PhoneNumberType] {
  override val values: immutable.IndexedSeq[PhoneNumberType] = findValues

  case object Local     extends PhoneNumberType("local")
  case object National  extends PhoneNumberType("national")
  case object Mobile    extends PhoneNumberType("mobile")
  case object TollFree  extends PhoneNumberType("tollfree")
  case object ShortCode extends PhoneNumberType("shortcode")

  private[client] def fromStringCaseInsensitive(s: String): PhoneNumberType = findValues
    .find(_.asString.toLowerCase === s.toLowerCase)
    .getOrElse(throw new IllegalArgumentException(s"$s is not a valid Twilio phone number type."))
}
