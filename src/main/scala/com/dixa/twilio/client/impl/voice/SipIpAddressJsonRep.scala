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

package com.dixa.twilio.client.impl.voice

import com.dixa.twilio.client.impl.Formatter
import com.dixa.twilio.client.impl.JsonParsingUtil.emptyStringToNone
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.{IpAccessControlList, SipIpAddress}

import java.time.Instant

final case class SipIpAddressJsonRep(
    account_sid: String,
    date_created: String,
    date_updated: String,
    friendly_name: String,
    ip_access_control_list_sid: String,
    ip_address: String,
    cidr_prefix_length: Option[String],
    sid: String
) {

  def toModelUnsafe: SipIpAddress = SipIpAddress(
    SipIpAddress.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    SipIpAddress.FriendlyName.unsafe(friendly_name),
    SipIpAddress.IpAddress.unsafe(ip_address),
    emptyStringToNone(cidr_prefix_length).map(SipIpAddress.CidrPrefixLength.fromTwilioStringUnsafe),
    IpAccessControlList.Sid.unsafe(ip_access_control_list_sid),
    Instant.from(Formatter.dateTime.parse(date_created)),
    Instant.from(Formatter.dateTime.parse(date_updated))
  )
}

private[voice] object SipIpAddressJsonRep {

  implicit val upickleReader: Reader[SipIpAddressJsonRep] =
    macroR[SipIpAddressJsonRep]
}
