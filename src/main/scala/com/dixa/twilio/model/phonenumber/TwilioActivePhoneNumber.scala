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

import com.dixa.twilio.model.iam.TwilioAccount

final case class TwilioActivePhoneNumber(
    sid: TwilioPhoneNumber.Sid,
    accountSid: TwilioAccount.Sid,
    phoneNumber: PhoneNumberE164,
    `type`: PhoneNumberType,
    lifecycle: PhoneNumberLifecycle,
    capabilities: PhoneNumberCapabilities,
    regulatory: PhoneNumberRegulatoryRequirement,
    geography: PhoneNumberGeography,
    // skipping "configuration" for now
) extends TwilioPhoneNumber
