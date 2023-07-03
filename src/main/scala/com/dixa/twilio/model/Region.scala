package com.dixa.twilio.model

/** Enum representing the different regions available in Twilio.
  *
  * Twilio Regions are isolated data centers around the world where Twilio performs the processing
  * and storage required to enable your application's communications activities.
  *
  * Note that public edge locations was once referred to as regions, but they are not the same.
  * However they do still have a reference to it, called LegacyRegion. See [[PublicEdgeLocation]]
  * for details.
  *
  * @see
  *   https://www.twilio.com/docs/global-infrastructure/understanding-twilio-regions
  */
sealed abstract class Region(override val toString: String) extends EnumWithTwilioString.EnumEntry

object Region extends EnumWithTwilioString[Region] {
  override def values: scala.collection.immutable.IndexedSeq[Region] = findValues

  case object Us1 extends Region("us1")
  case object Ie1 extends Region("ie1")
  case object De1 extends Region("de1")
  case object Sg1 extends Region("sg1")
  case object Br1 extends Region("br1")
  case object Au1 extends Region("au1")
  case object Jp1 extends Region("jp1")
}
