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

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.{SidAbstract, TwilioStringValue}

import java.time.{Duration, Instant}

final case class Queue(
    sid: Queue.Sid,
    friendlyName: Queue.FriendlyName,
    accountSid: TwilioAccount.Sid,
    currentSize: Queue.CurrentSize,
    maxSize: Queue.MaxSize,
    averageWaitTime: Duration,
    dateCreated: Instant,
    dateUpdated: Instant
)

object Queue {

  final case class Sid private[Queue] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("QU")), new Sid(_))

  final case class FriendlyName(override val toString: String) extends TwilioStringValue

  final case class CurrentSize(asInt: Int) extends TwilioStringValue {
    override def twilioString: String = asInt.toString
  }

  /** Represent the max size of the Queue.
    *
    * This value has to be between 1 and 5000. This class treat is as a Int, so that this library
    * would continue to be useable even if Twilio change that limit, but there is a Enumeration in
    * the companion object, representing all allowed values, that can be used if you want to force
    * valid values via the type system.
    */
  final case class MaxSize(asInt: Int) extends TwilioStringValue {
    override def twilioString: String = asInt.toString
  }

}
