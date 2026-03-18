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

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix

/** Base trait for the different kind of phone number entities in Twilio.
  */
trait TwilioPhoneNumber {

  // Common stuff should be moved down here, as we find the need for it.
}

object TwilioPhoneNumber {

  final case class Sid private[TwilioPhoneNumber] (override val toString: String)
      extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("PN")), new Sid(_))
}
