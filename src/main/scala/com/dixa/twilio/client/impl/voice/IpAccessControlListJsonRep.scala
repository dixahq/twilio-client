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

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.IpAccessControlList

import java.time.Instant

final case class IpAccessControlListJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    friendly_name: Option[String],
    sid: String,
) {

  def toModelUnsafe: IpAccessControlList = IpAccessControlList(
    TwilioAccount.Sid.unsafe(account_sid),
    IpAccessControlList.Sid.unsafe(sid),
    emptyStringToNone(friendly_name).map(IpAccessControlList.FriendlyName.unsafe),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[voice] object IpAccessControlListJsonRep {

  implicit val upickleReader: Reader[IpAccessControlListJsonRep] =
    macroR[IpAccessControlListJsonRep]
}
