package com.dixa.twilio.model.phonenumber

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

sealed abstract class PhoneNumberLifecycle(val apiName: String) extends EnumEntry

object PhoneNumberLifecycle extends Enum[PhoneNumberLifecycle] {
  override val values: immutable.IndexedSeq[PhoneNumberLifecycle] = findValues

  case object Beta               extends PhoneNumberLifecycle("beta")
  case object DeveloperPreview   extends PhoneNumberLifecycle("developer-preview")
  case object GenerallyAvailable extends PhoneNumberLifecycle("generally-available")
  case object Exhausted          extends PhoneNumberLifecycle("exhausted")

  def fromApiNameCaseInsensitive(s: String): PhoneNumberLifecycle = values
    .find(_.apiName.toLowerCase === s.toLowerCase)
    .getOrElse(
      throw new IllegalArgumentException(
        s"$s is not a valid PhoneNumberLifecycle. Valid values are: $values"
      )
    )
}
