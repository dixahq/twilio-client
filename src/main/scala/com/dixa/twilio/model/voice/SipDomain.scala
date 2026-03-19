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

package com.dixa.twilio.model.voice

import com.dixa.twilio.model._
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

import java.time.Instant

/** A SIP Domain resource describes a custom DNS hostname that can accept SIP traffic for your
  * account.
  *
  * A SIP request to that domain, such as to sip:alice@example.sip.twilio.com, routes to Twilio.
  * Twilio authenticates the request and requests TwiML from the voice_url of the SIP Domain.
  *
  * Note that a SipDomain has sub resources in form of the ip access control lists and credential
  * lists. These sub resources are not included in this type, as Twilio represent them as seperate
  * resources in there API.
  */
final case class SipDomain(
    accountSid: TwilioAccount.Sid,
    authType: Option[SipDomain.AuthType],
    dateCreated: Instant,
    dateUpdated: Instant,
    domainName: SipDomain.DomainName,
    friendlyName: Option[SipDomain.FriendlyName],
    sid: SipDomain.Sid,
    voiceFallbackUrl: CallbackUrlOptionalAndRequiredMethod[CallbackUrl.VoiceFallbackUrl],
    voiceStatusCallbackUrl: CallbackUrlOptionalAndRequiredMethod[
      CallbackUrl.VoiceStatusCallbackUrl
    ],
    voiceUrl: CallbackUrlOptionalAndRequiredMethod[CallbackUrl.VoiceUrl],
    sipRegistration: Boolean,
    emergencyCallingEnabled: Boolean,
    secure: Boolean,
    byocTrunkSid: Option[ByocTrunk.Sid],
    emergencyCallerSid: Option[TwilioPhoneNumber.Sid]
)

object SipDomain {

  final case class Sid private[SipDomain] (override val toString: String) extends SidAbstract
  object Sid extends SidAbstract.SidCompanionObject[Sid](List(SidAbstract.Prefix("SD")), new Sid(_))

  sealed abstract class AuthType extends EnumWithTwilioString.EnumEntry
  object AuthType                extends EnumWithTwilioString[AuthType] {
    override def values: IndexedSeq[AuthType] = findValues

    case object IpAcl          extends AuthType
    case object CredentialList extends AuthType

    case object IpAclAndCredentialList extends AuthType
  }

  final case class DomainName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue {

    /** Returns the SIP URI for this domain reached via the given Twilio edge location.
      *
      * For example, a domain `org-example.sip.twilio.com` with edge location
      * [[PublicEdgeLocation.Dublin]] becomes `org-example.sip.dublin.twilio.com`.
      *
      * @see
      *   https://www.twilio.com/docs/global-infrastructure/localized-uris/sip-uris
      */
    def withEdgeLocation(edge: PublicEdgeLocation): String = {
      val prefix = toString.dropRight(".sip.twilio.com".length)
      s"$prefix.sip.${edge.edgeId}.twilio.com"
    }

    /** Returns a map of all Twilio edge locations to their corresponding SIP URIs for this domain.
      *
      * @see
      *   [[withEdgeLocation]]
      */
    def allEdgeLocationUris: Map[PublicEdgeLocation, String] =
      PublicEdgeLocation.values.map(edge => edge -> withEdgeLocation(edge)).toMap
  }

  object DomainName extends ConstrainedString.ConstrainedStringCompanionObject[DomainName] {
    override protected def constructInstance(wrapped: String): DomainName = new DomainName(wrapped)

    override val validChars: Set[Char] = Set(
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
      't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
      'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4',
      '5', '6', '7', '8', '9', '-', '.'
    )

    override protected val requireSuffix: String = ".sip.twilio.com"
  }

  /** Friendly name of a SipDomain. */
  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue
  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName] {
    override protected def constructInstance(wrapped: String): FriendlyName = new FriendlyName(
      wrapped
    )

    override protected val maxLength: Option[Int] = Some(64)
  }
}
