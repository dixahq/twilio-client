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

package com.dixa.twilio.model.twiml.verb

import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.collection.immutable

/** Representation of the Redirect Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  */
sealed trait RedirectVerb extends TwimlElement.Verb {
  override final protected def tagName: String = "Redirect"

  override final protected def tagSubElements: immutable.Seq[TwimlElement] = Nil
}

object RedirectVerb {

  final class Builder[B <: TwimlConstraints.Buildable] private (
      callbackUrl: Option[CallbackUrl],
      method: Option[HttpMethod]
  ) {

    def withCallbackUrl(callbackUrl: CallbackUrl): Builder[TwimlConstraints.BuildableTrue] =
      new Builder(Some(callbackUrl), method)

    def withMethod(method: HttpMethod): Builder[B] = new Builder(callbackUrl, Some(method))

    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): RedirectVerb =
      RedirectVerbImpl(callbackUrl.get, method)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState(None, None)
  }

  type BuilderStartState = Builder[TwimlConstraints.BuildableFalse]
  type BuildFunction     = BuilderStartState => RedirectVerb

  def build(fun: BuildFunction): RedirectVerb = fun(Builder.empty)

  private final case class RedirectVerbImpl(callbackUrl: CallbackUrl, method: Option[HttpMethod])
      extends RedirectVerb {

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .add("method", method)
        .build

    override protected def tagValue: Option[String] = Some(callbackUrl.twilioString)
  }

}
