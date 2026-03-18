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

import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.twiml.TwimlConstraints.{Buildable, BuildableFalse, BuildableTrue}
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.{TwimlConstraints, TwimlElement}

import scala.collection.immutable

/** Representation of the Play Verb from TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * Twilio documentation: https://www.twilio.com/docs/voice/twiml/play
  */
sealed trait PlayVerb extends TwimlElement.Verb {
  override final protected def tagName: String = "Play"

  override final protected def tagSubElements: immutable.Seq[TwimlElement] = Nil
}

object PlayVerb {

  // Play specific constraints (Phantom types)
  sealed trait SoundFileAdded
  sealed trait SoundFileAddedTrue  extends SoundFileAdded
  sealed trait SoundFileAddedFalse extends SoundFileAdded

  sealed trait DigitsAdded
  sealed trait DigitsAddedTrue  extends DigitsAdded
  sealed trait DigitsAddedFalse extends DigitsAdded

  sealed trait LoopAdded
  sealed trait LoopAddedTrue  extends LoopAdded
  sealed trait LoopAddedFalse extends LoopAdded

  final class Builder[
      B <: Buildable,
      S <: SoundFileAdded,
      D <: DigitsAdded,
      L <: LoopAdded
  ] private (
      url: String,
      digits: Option[DtmfString],
      loopValue: Option[Int]
  ) {

    /** Add a url for a sound file to play.
      *
      * Only a single call to this method is allowed, as the Play verb only support a single file.
      * But you can just use two consecutive Play verbs, if you need to play two files in a row.
      *
      * You can add both this and [[withDigits]], and in such cases the digits are played before the
      * sound file.
      */
    def withSoundFileUrl(url: String)(
        implicit ev: S =:= SoundFileAddedFalse
    ): Builder[BuildableTrue, SoundFileAddedTrue, D, L] =
      new Builder[BuildableTrue, SoundFileAddedTrue, D, L](url = url, digits, loopValue)

    /** Add DTMF digits to play
      *
      * Only a single call to this method is allowed, as the Play verb only support one single
      * string of DTMF digits.
      *
      * You can add both this and [[withSoundFileUrl]], and in such cases the digits are played
      * before the sound file.
      */
    def withDigits(dtmfString: DtmfString)(
        implicit ev: D =:= DigitsAddedFalse
    ): Builder[BuildableTrue, S, DigitsAddedTrue, L] =
      new Builder[BuildableTrue, S, DigitsAddedTrue, L](url, Some(dtmfString), loopValue)

    /** Add loop attribute to the play verb.
      *
      * Will make Twilio loop it. Input value must be 0 or positive, otherwise it will fail at
      * runtime in Twilio.
      *
      * 0 will make Twilio loop it 1000 times, or until the call is hang up.
      */
    def withLoop(loopValue: Int)(
        implicit ev: L =:= LoopAddedFalse
    ): Builder[B, S, D, LoopAddedTrue] =
      new Builder[B, S, D, LoopAddedTrue](url, digits, Some(loopValue))

    def build()(
        implicit ev: B =:= TwimlConstraints.BuildableTrue
    ): PlayVerb = PlayVerbImpl(url, digits, loopValue)
  }

  object Builder {
    val empty: BuilderStartState = new BuilderStartState("", None, None)
  }

  type BuilderStartState =
    Builder[BuildableFalse, SoundFileAddedFalse, DigitsAddedFalse, LoopAddedFalse]
  type BuildFunction = BuilderStartState => PlayVerb

  def build(fun: BuildFunction): PlayVerb = fun(Builder.empty)

  private final case class PlayVerbImpl(
      url: String,
      digits: Option[DtmfString],
      loopValue: Option[Int]
  ) extends PlayVerb {

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .add("digits", digits)
        .addInt("loop", loopValue)
        .build

    /** Specify the value the tag this TwiML element represent has.
      *
      * This is used when building the XML of the TwiMLElement.
      */
    override protected def tagValue: Option[String] = Some(url)

  }
}
