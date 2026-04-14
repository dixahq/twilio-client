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

package com.dixa.twilio.client.impl.phonenumber

import com.dixa.twilio.client.impl.phonenumber.IncomingPhoneNumberJsonRep._
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioIncomingPhoneNumber.PhoneNumberCapabilitiesSummary
import com.dixa.twilio.model.phonenumber.{
  PhoneNumberE164,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumber
}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.Region

private[phonenumber] final case class IncomingPhoneNumberJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: String,
    phone_number: String,
    capabilities: IncomingNumberCapabilitiesJsonRep,
) {

  private[phonenumber] def toModel(region: Region) = TwilioIncomingPhoneNumber(
    TwilioPhoneNumber.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    TwilioIncomingPhoneNumber.FriendlyName(friendly_name),
    PhoneNumberE164.unsafe(phone_number),
    PhoneNumberCapabilitiesSummary(
      capabilities.voice,
      capabilities.sms,
      capabilities.mms,
      capabilities.fax.getOrElse(false)
    ),
    region
  )
}

private[phonenumber] object IncomingPhoneNumberJsonRep {
  final case class IncomingNumberCapabilitiesJsonRep(
      voice: Boolean,
      sms: Boolean,
      mms: Boolean,
      fax: Option[Boolean] = None,
  )

  private implicit val incomingNumberCapabilitiesJsonRepReader
      : Reader[IncomingNumberCapabilitiesJsonRep] =
    macroR[IncomingNumberCapabilitiesJsonRep]

  implicit val incomingPhoneNumberJsonRepReader: Reader[IncomingPhoneNumberJsonRep] =
    macroR[IncomingPhoneNumberJsonRep]
}
