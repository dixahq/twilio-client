// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

  def fromLegacyRegion(region: LegacyRegion): PublicEdgeLocation = region match {
    case LegacyRegion.Au1 => Sydney
    case LegacyRegion.Br1 => SaoPaulo
    case LegacyRegion.Ie1 => Dublin
    case LegacyRegion.De1 => Frankfurt
    case LegacyRegion.Jp1 => Tokyo
    case LegacyRegion.Sg1 => Singapore
    case LegacyRegion.Us1 => Ashburn
    case LegacyRegion.Us2 => Umatilla
    case LegacyRegion.Gll => Roaming
  }

  def fromLegacyRegionCode(code: String): Option[PublicEdgeLocation] =
    LegacyRegion.values.find(_.id == code).map(fromLegacyRegion)

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
