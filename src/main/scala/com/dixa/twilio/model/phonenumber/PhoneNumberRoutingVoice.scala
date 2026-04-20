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

import com.dixa.twilio.model.SidAbstract
import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount

/** The routing assigned to a phone number in Twilio's Voice Routing API.
  *
  * @param sid
  *   The unique string identifying the phone number routing resource.
  * @param accountSid
  *   The SID of the account that owns the phone number.
  * @param phoneNumber
  *   The phone number in E.164 format.
  * @param friendlyName
  *   A human-readable name for the phone number.
  * @param voiceRegion
  *   The Twilio region identifier that handles voice traffic for this number (e.g. "us1", "ie1",
  *   "au1").
  *
  * @see
  *   https://www.twilio.com/docs/global-infrastructure/inbound-processing-region-api-phone-number#fetch-a-phonenumbers-current-inbound-processing-region-configuration
  */
final case class PhoneNumberRoutingVoice(
    sid: PhoneNumberRoutingVoice.Sid,
    accountSid: TwilioAccount.Sid,
    phoneNumber: PhoneNumberE164,
    friendlyName: String,
    voiceRegion: String
)

object PhoneNumberRoutingVoice {

  final case class Sid private[PhoneNumberRoutingVoice] (override val toString: String)
      extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("QQ")), new Sid(_))
}
