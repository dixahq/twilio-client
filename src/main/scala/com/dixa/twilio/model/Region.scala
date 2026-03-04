package com.dixa.twilio.model

/** Enum representing the different regions available in Twilio.
  *
  * Twilio Regions are isolated data centers around the world where Twilio performs the processing
  * and storage required to enable your application's communications activities.
  *
  * Note that public edge locations were once referred to as regions, but they are different. Today
  * [[PublicEdgeLocation]] is what was then called regions.
  *
  * Note that not all regions have the same features available.
  *
  * @see
  *   https://www.twilio.com/docs/global-infrastructure/understanding-twilio-regions
  * @see
  *   https://www.twilio.com/docs/global-infrastructure/regional-product-and-feature-availability
  */
sealed abstract class Region(override val toString: String) extends EnumWithTwilioString.EnumEntry

object Region extends EnumWithTwilioString[Region] {
  override def values: scala.collection.immutable.IndexedSeq[Region] = findValues

  case object Us1        extends Region("us1")
  case object Ireland1   extends Region("ie1")
  case object Australia1 extends Region("au1")
}
