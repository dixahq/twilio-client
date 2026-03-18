// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
