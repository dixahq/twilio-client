package com.dixa.twilio.client.model.phonenumber

import enumeratum.{Enum, EnumEntry}
import org.scalactic.TypeCheckedTripleEquals._

import scala.collection.immutable

sealed abstract class PhoneNumberLifecycle(private[client] val asString: String) extends EnumEntry

object PhoneNumberLifecycle extends Enum[PhoneNumberLifecycle] {
  override val values: immutable.IndexedSeq[PhoneNumberLifecycle] = findValues

  case object Beta               extends PhoneNumberLifecycle("beta")
  case object DeveloperPreview   extends PhoneNumberLifecycle("developer-preview")
  case object GenerallyAvailable extends PhoneNumberLifecycle("generally-available")
  case object Exhausted          extends PhoneNumberLifecycle("exhausted")

  private[client] def fromStringCaseInsensitive(s: String): PhoneNumberLifecycle = findValues
    .find(_.asString.toLowerCase === s.toLowerCase)
    .getOrElse(throw new IllegalArgumentException(s"$s is not a valid PhoneNumberLifecycle."))
}
