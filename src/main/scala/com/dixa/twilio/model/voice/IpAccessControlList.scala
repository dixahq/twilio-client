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
import com.dixa.twilio.model.{ConstrainedString, SidAbstract, TwilioStringValue}

import java.time.Instant

/** IpAccessControlList resources contain the Access Control List (ACL), which is a list of
  * IpAddress resources that describe the IP addresses with access to the SIP Domain. Requests to
  * the SIP Domain from an IP address that is not in the ACL are blocked.
  *
  * After you create an IpAccessControlList resource, you will need to map it to your SIP domain for
  * it to take effect. You can apply the same list to more than one SIP Domain.
  *
  * Your Account can have up to 1,000 IpAccessControlList resources. Each IpAccessControlList
  * resource can contain up to 100 entries (which could be CIDR blocks).
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaccesscontrollist-resource
  */
final case class IpAccessControlList(
    accountSid: TwilioAccount.Sid,
    sid: IpAccessControlList.Sid,
    friendlyName: Option[IpAccessControlList.FriendlyName],
    dateCreated: Instant,
    dateUpdated: Instant
)

object IpAccessControlList {
  final case class Sid private[IpAccessControlList] (override val toString: String)
      extends SidAbstract
  object Sid extends SidAbstract.SidCompanionObject[Sid](List(SidAbstract.Prefix("AL")), new Sid(_))

  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName] {
    override protected def constructInstance(wrapped: String): FriendlyName = new FriendlyName(
      wrapped
    )

    override protected val maxLength: Option[Int] = Some(255)
  }
}
