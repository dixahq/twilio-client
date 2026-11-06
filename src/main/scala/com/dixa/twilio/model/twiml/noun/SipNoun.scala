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

import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.verb.DialVerb
import com.dixa.twilio.model.{ConstrainedString, TwilioStringValue}

import scala.collection.immutable

/** Represent the Sip noun in TwiML: a SIP endpoint for the Dial verb to call.
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/sip
  */
sealed trait SipNoun extends TwimlElement.Noun with DialVerb.DestinationNoun

object SipNoun {

  /** The SIP URI to call, such as `sip:jack@example.com`.
    *
    * Twilio requires the URI to be under 255 chars.
    */
  final case class Uri private (override val toString: String)
      extends ConstrainedString
      with TwilioStringValue

  object Uri extends ConstrainedString.ConstrainedStringCompanionObject[Uri] {
    override protected def constructInstance(wrapped: String): Uri = new Uri(wrapped)

    override protected val maxLength: Option[Int] = Some(254)
  }

  /** The username that Twilio sends when the SIP endpoint asks for authentication. */
  final case class Username(override val toString: String) extends TwilioStringValue

  /** The password that Twilio sends when the SIP endpoint asks for authentication.
    *
    * toString hides the password, so that it is not logged by accident.
    */
  final case class Password(asString: String) extends TwilioStringValue {
    override def twilioString: String = asString

    override def toString: String = "SipNoun.Password(***)"
  }

  final class Builder[B <: Buildable] private (
      uri: Option[Uri],
      username: Option[Username],
      password: Option[Password]
  ) {

    def withUri(uri: Uri): Builder[BuildableTrue] = new Builder(Some(uri), username, password)

    /** Set the username and password that Twilio sends when the SIP endpoint asks for
      * authentication.
      */
    def withCredentials(username: Username, password: Password): Builder[B] =
      new Builder(uri, Some(username), Some(password))

    def build()(
        implicit ev: B =:= BuildableTrue
    ): SipNoun = SipNounImpl(uri.get, username, password)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState(None, None, None)
  }

  type BuilderStartState = Builder[BuildableFalse]
  type BuildFunction     = BuilderStartState => SipNoun

  def build(fun: BuildFunction): SipNoun = fun(Builder.empty)

  private final case class SipNounImpl(
      uri: Uri,
      username: Option[Username],
      password: Option[Password]
  ) extends SipNoun {

    override protected def tagName: String = "Sip"

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .add("username", username)
        .add("password", password)
        .build

    override protected def tagSubElements: immutable.Seq[TwimlElement] = Nil

    override protected def tagValue: Option[String] = Some(uri.twilioString)
  }
}
