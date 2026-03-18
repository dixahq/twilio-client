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

import com.dixa.twilio.model.{
  ConstrainedString,
  EnumWithTwilioString,
  SidAbstract,
  TwilioStringValue
}
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** IpAddress resources describe the IP addresses that have access to the SIP Domain.
  *
  * @see
  *   https://www.twilio.com/docs/voice/sip/api/sip-ipaddress-resource#maincontent
  */
final case class SipIpAddress(
    sid: SipIpAddress.Sid,
    accountSid: TwilioAccount.Sid,
    friendlyName: SipIpAddress.FriendlyName,
    ipAddress: SipIpAddress.IpAddress,
    cidrPrefixLength: Option[SipIpAddress.CidrPrefixLength],
    ipAccessControlListSid: IpAccessControlList.Sid,
    dateCreated: Instant,
    dateUpdated: Instant
)

object SipIpAddress {

  final case class Sid private[SipIpAddress] (override val toString: String) extends SidAbstract
  object Sid extends SidAbstract.SidCompanionObject[Sid](List(SidAbstract.Prefix("IP")), new Sid(_))

  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName] {
    override protected def constructInstance(wrapped: String): FriendlyName = new FriendlyName(
      wrapped
    )

    override protected val maxLength: Option[Int] = Some(255)
  }

  final case class IpAddress private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object IpAddress extends ConstrainedString.ConstrainedStringCompanionObject[IpAddress] {
    override protected def constructInstance(wrapped: String): IpAddress = new IpAddress(wrapped)

    override protected val ipv4Only: Boolean = true
  }

  sealed abstract class CidrPrefixLength extends EnumWithTwilioString.EnumEntry
  object CidrPrefixLength                extends EnumWithTwilioString[CidrPrefixLength] {
    override def values: IndexedSeq[CidrPrefixLength] = findValues

    case object `0`  extends CidrPrefixLength
    case object `1`  extends CidrPrefixLength
    case object `2`  extends CidrPrefixLength
    case object `3`  extends CidrPrefixLength
    case object `4`  extends CidrPrefixLength
    case object `5`  extends CidrPrefixLength
    case object `6`  extends CidrPrefixLength
    case object `7`  extends CidrPrefixLength
    case object `8`  extends CidrPrefixLength
    case object `9`  extends CidrPrefixLength
    case object `10` extends CidrPrefixLength
    case object `11` extends CidrPrefixLength
    case object `12` extends CidrPrefixLength
    case object `13` extends CidrPrefixLength
    case object `14` extends CidrPrefixLength
    case object `15` extends CidrPrefixLength
    case object `16` extends CidrPrefixLength
    case object `17` extends CidrPrefixLength
    case object `18` extends CidrPrefixLength
    case object `19` extends CidrPrefixLength
    case object `20` extends CidrPrefixLength
    case object `21` extends CidrPrefixLength
    case object `22` extends CidrPrefixLength
    case object `23` extends CidrPrefixLength
    case object `24` extends CidrPrefixLength
    case object `25` extends CidrPrefixLength
    case object `26` extends CidrPrefixLength
    case object `27` extends CidrPrefixLength
    case object `28` extends CidrPrefixLength
    case object `29` extends CidrPrefixLength
    case object `30` extends CidrPrefixLength
    case object `31` extends CidrPrefixLength
    case object `32` extends CidrPrefixLength
  }

}
