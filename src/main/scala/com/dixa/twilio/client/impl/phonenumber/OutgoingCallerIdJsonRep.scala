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

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.model.phonenumber._
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

private[phonenumber] final case class OutgoingCallerIdJsonRep(
    sid: String,
    account_sid: String,
    phone_number: String,
    friendly_name: Option[String],
    date_created: String,
    date_updated: String,
) {

  private[phonenumber] def toModel = OutgoingCallerId(
    sid = OutgoingCallerId.Sid.unsafe(sid),
    accountSid = TwilioAccount.Sid.unsafe(account_sid),
    friendlyName =
      emptyStringToNone(friendly_name).map(OutgoingCallerId.FriendlyName.constructInstance),
    phoneNumber = PhoneNumberE164.unsafe(phone_number),
    dateCreated = Instant.from(Formatter.dateTime.parse(date_created)),
    dateUpdated = Instant.from(Formatter.dateTime.parse(date_updated)),
  )
}

private[phonenumber] object OutgoingCallerIdJsonRep {

  implicit val outgoingCallerIdJsonRepReader: Reader[OutgoingCallerIdJsonRep] =
    macroR[OutgoingCallerIdJsonRep]
}
