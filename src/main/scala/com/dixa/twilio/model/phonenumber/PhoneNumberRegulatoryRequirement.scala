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

package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.phonenumber.PhoneNumberRegulatoryRequirement._

import scala.collection.immutable

final case class PhoneNumberRegulatoryRequirement(
    addressRequirement: AddressRequirementType,
)

object PhoneNumberRegulatoryRequirement {
  sealed abstract class AddressRequirementType(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object AddressRequirementType extends EnumWithTwilioString[AddressRequirementType] {
    override val values: immutable.IndexedSeq[AddressRequirementType] = findValues

    case object Any     extends AddressRequirementType("any")
    case object Local   extends AddressRequirementType("local")
    case object Foreign extends AddressRequirementType("foreign")
    case object None    extends AddressRequirementType("none")
  }

}
