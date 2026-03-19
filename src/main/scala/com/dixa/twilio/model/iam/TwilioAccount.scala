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

package com.dixa.twilio.model.iam

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract, TwilioStringValue}

import java.time.Instant
import scala.collection.immutable

/** Represent a Account or a Subaccount at Twilio
  */
final case class TwilioAccount(
    name: TwilioAccount.Name,
    sid: TwilioAccount.Sid,
    status: TwilioAccount.Status,
    /** Sid of the owning account. In case of a root account, value will be same as sid */
    ownerAccountSid: TwilioAccount.Sid,
    authToken: AuthToken.Primary,
    accountType: TwilioAccount.Type,
    timeCreated: Instant,
    timeUpdated: Instant
) {
  def isActive: Boolean = status == TwilioAccount.Status.Active
}

object TwilioAccount {

  final case class Name(override val toString: String)         extends TwilioStringValue
  final case class Sid private (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject[Sid](List(Prefix("AC")), new Sid(_))

  sealed abstract class Status(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Active    extends Status("active")
    case object Suspended extends Status("suspended")
    case object Closed    extends Status("closed")
  }

  sealed abstract class Type(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry
  object Type extends EnumWithTwilioString[Type] {
    override val values: immutable.IndexedSeq[Type] = findValues

    case object Trail extends Type("Trial")
    case object Full  extends Type("Full")
  }
}
