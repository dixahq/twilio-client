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

package com.dixa.twilio.model.phonenumber

import com.dixa.twilio.model.EnumWithTwilioString

import scala.collection.immutable

sealed abstract class PhoneNumberLifecycle(override val twilioString: String)
    extends EnumWithTwilioString.EnumEntry

object PhoneNumberLifecycle extends EnumWithTwilioString[PhoneNumberLifecycle] {
  override val values: immutable.IndexedSeq[PhoneNumberLifecycle] = findValues

  case object Beta               extends PhoneNumberLifecycle("beta")
  case object DeveloperPreview   extends PhoneNumberLifecycle("developer-preview")
  case object GenerallyAvailable extends PhoneNumberLifecycle("generally-available")
  case object Exhausted          extends PhoneNumberLifecycle("exhausted")
}
