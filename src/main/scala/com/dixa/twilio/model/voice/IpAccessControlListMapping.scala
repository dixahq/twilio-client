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

package com.dixa.twilio.model.voice

import com.dixa.twilio.model.iam.TwilioAccount

/** Represent a mapping between a [[SipDomain]] and a [[IpAccessControlList]].
  *
  * At point of writing this, the Twilio documentation does not correspond exactly to how their API
  * actually represent this. Because of this, this class diverge a bit from the Twilio doc on these
  * points:
  *   - This resource does not have its own sid. The sid parameter is always the value of the
  *     SipDomains, and as such this class calls it SipDomainSid.
  *   - Twilio does not seem to record individual timestamp on the mappings, and instead the
  *     resource always returns the timestamps of the parent SipDomain resource. Because of that,
  *     this class has no timestamps.
  *   - There is no friendly name, as Twilio does not record a friendly name specific for this sub
  *     resource. The friendly name returned by the Twilio API is the friendly name of the linked
  *     [[IpAccessControlList]]
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollistmapping-resource
  */
final case class IpAccessControlListMapping(
    accountSid: TwilioAccount.Sid,
    sipDomainSid: SipDomain.Sid,
    ipAccessControlListSid: IpAccessControlList.Sid
)
