package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithApiName

import scala.collection.immutable

sealed abstract class PhoneNumberLifecycle(val apiName: String) extends EnumWithApiName.EnumEntry

object PhoneNumberLifecycle extends EnumWithApiName[PhoneNumberLifecycle] {
  override val values: immutable.IndexedSeq[PhoneNumberLifecycle] = findValues

  case object Beta               extends PhoneNumberLifecycle("beta")
  case object DeveloperPreview   extends PhoneNumberLifecycle("developer-preview")
  case object GenerallyAvailable extends PhoneNumberLifecycle("generally-available")
  case object Exhausted          extends PhoneNumberLifecycle("exhausted")
}
