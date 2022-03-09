package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithApiName
import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

sealed abstract class PhoneNumberType(val apiName: String) extends EnumWithApiName.EnumEntry

object PhoneNumberType extends EnumWithApiName[PhoneNumberType] {
  override val values: immutable.IndexedSeq[PhoneNumberType] = findValues

  case object Local     extends PhoneNumberType("local")
  case object National  extends PhoneNumberType("national")
  case object Mobile    extends PhoneNumberType("mobile")
  case object TollFree  extends PhoneNumberType("tollfree")
  case object ShortCode extends PhoneNumberType("shortcode")
}
