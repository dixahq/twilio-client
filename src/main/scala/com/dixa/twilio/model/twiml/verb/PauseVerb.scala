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

import com.dixa.twilio.model.PositiveInteger
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder

import java.time.Duration
import scala.collection.immutable

/** Representation of the Pause Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/pause
  */
sealed trait PauseVerb extends TwimlElement.Verb {
  override final protected val tagName: String = "Pause"

  override final protected val tagSubElements: immutable.Seq[TwimlElement] = Nil

  override final protected val tagValue: Option[String] = None
}

object PauseVerb {
  final class Builder private (length: Option[Long] = None) {

    def withLengthInSeconds(length: PositiveInteger): Builder =
      new Builder(Some(length.int.longValue()))

    def withLength(length: Duration): Builder =
      new Builder(Some(length.getSeconds))

    def build(): PauseVerb = PauseVerbImpl(length)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState
  }

  type BuilderStartState = Builder
  type BuildFunction     = BuilderStartState => PauseVerb

  def build(fun: BuildFunction): PauseVerb = fun(Builder.empty)

  private final case class PauseVerbImpl(lengthInSeconds: Option[Long]) extends PauseVerb {

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .addLong("length", lengthInSeconds)
        .build
  }
}
