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

import com.dixa.twilio.model.{ConstrainedString, SidAbstract, TwilioStringValue}
import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount

import java.time.Instant

/** Represents a outgoing caller id
  *
  * An OutgoingCallerId instance resource represents a single verified number that may be used as a
  * caller ID when making outgoing calls
  *
  * @see
  *   https://www.twilio.com/docs/voice/api/outgoing-caller-ids#outgoingcallerid-instance-resource
  */
final case class OutgoingCallerId(
    sid: OutgoingCallerId.Sid,
    accountSid: TwilioAccount.Sid,
    friendlyName: Option[OutgoingCallerId.FriendlyName],
    phoneNumber: PhoneNumberE164,
    dateCreated: Instant,
    dateUpdated: Instant,
)

object OutgoingCallerId {

  final case class Sid private[OutgoingCallerId] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("PN")), new Sid(_))

  final case class FriendlyName private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue

  object FriendlyName extends ConstrainedString.ConstrainedStringCompanionObject[FriendlyName] {
    override def constructInstance(wrapped: String) = new FriendlyName(wrapped)

    override protected val maxLength: Option[Int] = Some(64)
  }
}
