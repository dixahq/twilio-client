package com.dixa.twilio.model

import scala.collection.immutable

sealed abstract class Region(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry

object Region extends EnumWithTwilioString[Region] {
  override val values: immutable.IndexedSeq[Region] = findValues

  case object Us1 extends Region("us1")
  case object Ie1 extends Region("ie1")
  case object De1 extends Region("de1")
  case object Sg1 extends Region("sg1")
  case object Br1 extends Region("br1")
  case object Au1 extends Region("au1")
  case object Jp1 extends Region("jp1")
}
