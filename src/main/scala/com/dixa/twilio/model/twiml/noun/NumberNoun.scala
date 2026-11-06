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

package com.dixa.twilio.model.twiml.noun

import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.verb.DialVerb

import scala.collection.immutable

/** Represent the Number noun in TwiML: a phone number for the Dial verb to call.
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/number
  */
sealed trait NumberNoun extends TwimlElement.Noun with DialVerb.DestinationNoun

object NumberNoun {

  final class Builder[B <: Buildable] private (phoneNumber: Option[PhoneNumberE164]) {

    def withPhoneNumber(phoneNumber: PhoneNumberE164): Builder[BuildableTrue] =
      new Builder(Some(phoneNumber))

    def build()(
        implicit ev: B =:= BuildableTrue
    ): NumberNoun = NumberNounImpl(phoneNumber.get)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState(None)
  }

  type BuilderStartState = Builder[BuildableFalse]
  type BuildFunction     = BuilderStartState => NumberNoun

  def build(fun: BuildFunction): NumberNoun = fun(Builder.empty)

  private final case class NumberNounImpl(phoneNumber: PhoneNumberE164) extends NumberNoun {

    override protected def tagName: String = "Number"

    override protected def tagAttributes: immutable.Seq[(String, String)] = Nil

    override protected def tagSubElements: immutable.Seq[TwimlElement] = Nil

    override protected def tagValue: Option[String] = Some(phoneNumber.asString)
  }
}
