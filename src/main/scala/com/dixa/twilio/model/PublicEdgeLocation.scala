package com.dixa.twilio.model

import scala.collection.immutable

/** Represents a public edge location in Twilio's network. An edge location is a physical location
  * where Twilio's infrastructure is deployed, and can be used as an entry point for connections,
  * from where they will be routed to the [[Region]] data center via Twilio internal network.
  */
sealed abstract class PublicEdgeLocation(
    val edgeId: String,

    /** Will be set in case this edge location had a legacy region id, from when edge locations were
      * called regions.
      *
      * Before Twilio introduced the current region and edge location setup, they called what is
      * edge locations today for regions, and the current regions did not exists at all.. The old
      * regions were represented by a code like au1, br1, etc. These are deprecated as today and are
      * planned to be removed in the future. In this library we call them legacyRegionId, and is
      * represented as this optional field on a PublicEdgeLocation.
      */
    val legacyRegionId: Option[PublicEdgeLocation.LegacyRegion]
) extends EnumWithTwilioString.EnumEntry {

  def debugToString = s"TwilioPublicEdgeLocation.${this.toString}($edgeId, $legacyRegionId)"
}

object PublicEdgeLocation extends EnumWithTwilioString[PublicEdgeLocation] {

  override def values: immutable.IndexedSeq[PublicEdgeLocation] = findValues

  case object Sydney    extends PublicEdgeLocation("sydney", Some(LegacyRegion.Au1))
  case object SaoPaulo  extends PublicEdgeLocation("sao-paulo", Some(LegacyRegion.Br1))
  case object Dublin    extends PublicEdgeLocation("dublin", Some(LegacyRegion.Ie1))
  case object Frankfurt extends PublicEdgeLocation("frankfurt", Some(LegacyRegion.De1))
  case object Tokyo     extends PublicEdgeLocation("tokyo", Some(LegacyRegion.Jp1))
  case object Singapore extends PublicEdgeLocation("singapore", Some(LegacyRegion.Sg1))
  case object Ashburn   extends PublicEdgeLocation("ashburn", Some(LegacyRegion.Us1))
  case object Umatilla  extends PublicEdgeLocation("umatilla", Some(LegacyRegion.Us2))
  case object Roaming   extends PublicEdgeLocation("roaming", Some(LegacyRegion.Gll))

  def withEdgeOrRegionIdOption(s: String): Option[PublicEdgeLocation] = {
    values.find(a => a.edgeId == s || a.legacyRegionId.exists(_.id == s))
  }

  def withEdgeOrRegionId(s: String): PublicEdgeLocation = {
    values.find(a => a.edgeId == s || a.legacyRegionId.exists(_.id == s)) match {
      case Some(r) => r
      case None    => throw new NoSuchElementException(s"$s is not a member of Region enum")
    }
  }

  sealed abstract class LegacyRegion(val id: String) extends EnumWithTwilioString.EnumEntry
  object LegacyRegion                                extends EnumWithTwilioString[LegacyRegion] {

    override def values: immutable.IndexedSeq[LegacyRegion] = findValues

    case object Au1 extends LegacyRegion("au1")
    case object Br1 extends LegacyRegion("br1")
    case object Ie1 extends LegacyRegion("ie1")
    case object De1 extends LegacyRegion("de1")
    case object Jp1 extends LegacyRegion("jp1")
    case object Sg1 extends LegacyRegion("sg1")
    case object Us1 extends LegacyRegion("us1")
    case object Us2 extends LegacyRegion("us2")
    case object Gll extends LegacyRegion("gll")
  }
}
