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

package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.twiml.TwimlElement

import scala.collection.immutable

/** Representation of the Reject Verb from TwiML.
  *
  * The <Reject> verb rejects an incoming call to your Twilio number without billing you. This is
  * very useful for blocking unwanted calls.
  *
  * If the first verb in a TwiML document is <Reject>, Twilio will not pick up the call. The call
  * ends with a status of busy or no-answer, depending on the verb’s reason attribute. Any verbs
  * after <Reject> are unreachable and ignored.
  *
  * Using <Reject> as the first verb in your response is the only way to prevent Twilio from
  * answering a call. Any other response will result in an answered call and your account will be
  * billed.
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/reject
  */
sealed trait RejectVerb extends TwimlElement.Verb {
  override final protected val tagName: String = "Reject"

  override final protected val tagSubElements: immutable.Seq[TwimlElement] = Nil

  override final protected val tagValue: Option[String] = None
}

object RejectVerb {

  sealed abstract class RejectReason(override val toString: String)
      extends EnumWithTwilioString.EnumEntry
  object RejectReason extends EnumWithTwilioString[RejectReason] {
    override def values: immutable.IndexedSeq[RejectReason] = findValues

    case object Rejected extends RejectReason("rejected")
    case object Busy     extends RejectReason("busy")
  }

  final class Builder private (rejectReason: Option[RejectReason] = None) {

    /** Add reason for rejecting call.
      *
      * The reason attribute takes the values rejected and busy. This tells Twilio what message to
      * play when rejecting a call. Selecting busy will play a busy signal to the caller, while
      * selecting rejected will play a standard not-in-service response. The default is rejected.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/reject#attributes-reason
      */
    def withReason(rejectReason: RejectReason): Builder = new Builder(Some(rejectReason))

    def build(): RejectVerb = RejectVerbImpl(rejectReason)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => RejectVerb

  def build(fun: BuildFunction): RejectVerb = fun(Builder.empty)

  private final case class RejectVerbImpl(rejectReason: Option[RejectReason]) extends RejectVerb {

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      rejectReason.map(r => List("reason" -> r.twilioString)).getOrElse(Nil)
  }

}
