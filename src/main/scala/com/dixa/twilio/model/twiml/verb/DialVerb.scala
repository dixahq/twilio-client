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

import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.model.twiml.TwimlConstraints._
import com.dixa.twilio.model.twiml.TwimlElement
import com.dixa.twilio.model.twiml.TwimlElement.TagAttributeBuilder
import com.dixa.twilio.model.twiml.noun.ConferenceNoun
import com.dixa.twilio.model.voice.{AlphanumericCallerId, ClientIdentity}
import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod, TwilioStringValue}

import scala.collection.immutable

/** Represent the Dial verb in TwiML
  *
  * Creating a [[com.dixa.twilio.model.twiml.Response]] via the
  * [[com.dixa.twilio.model.twiml.Response.build]] method, is the preferred way to use this trait.
  *
  * @see
  *   https://www.twilio.com/docs/voice/twiml/dial
  */
trait DialVerb extends TwimlElement.Verb {
  override final protected def tagName: String = "Dial"
}

object DialVerb {

  /** Base of the nouns that Twilio can dial together in one Dial verb: the Number, Sip and Client
    * nouns.
    *
    * The constructor is private to the twiml package, so code outside this library cannot add its
    * own noun to a Dial verb.
    */
  abstract class DestinationNoun private[twiml] () extends TwimlElement.Noun

  /** DialVerb specific phantom types, used to enforce build constraints compile time. */
  object PhantomTypes {
    sealed trait RecordSet
    sealed trait RecordSetTrue  extends RecordSet
    sealed trait RecordSetFalse extends RecordSet

    sealed trait RecordingStatusCallbackSet
    sealed trait RecordingStatusCallbackSetTrue  extends RecordingStatusCallbackSet
    sealed trait RecordingStatusCallbackSetFalse extends RecordingStatusCallbackSet
  }

  /** How Twilio records the dialed call.
    *
    * There is no value for not recording, as that is what Twilio does when the attribute is absent.
    */
  sealed abstract class Record(override val toString: String) extends EnumWithTwilioString.EnumEntry

  object Record extends EnumWithTwilioString[Record] {
    override def values: immutable.IndexedSeq[Record] = findValues

    case object RecordFromAnswer      extends Record("record-from-answer")
    case object RecordFromRinging     extends Record("record-from-ringing")
    case object RecordFromAnswerDual  extends Record("record-from-answer-dual")
    case object RecordFromRingingDual extends Record("record-from-ringing-dual")
  }

  /** How many seconds Twilio lets the dialed party ring, before the Dial ends as not answered.
    *
    * Twilio allows `minSeconds` to `maxSeconds` seconds, and uses 30 seconds when the attribute is
    * absent.
    */
  final class Timeout private (val seconds: Int) extends TwilioStringValue {
    override def toString: String = seconds.toString
  }

  object Timeout {

    val minSeconds: Int = 5

    val maxSeconds: Int = 600

    sealed trait Err extends RuntimeException
    object Err {
      final case class NumberOutOfRange(seconds: Long)
          extends IllegalArgumentException(
            s"$seconds is not between $minSeconds and $maxSeconds seconds " +
              "as required for the timeout of a Dial."
          )
          with Err
    }

    /** The seconds are a Long, so that a value too large for an Int is out of range as well. */
    def safe(seconds: Long): Either[Err, Timeout] =
      if (seconds < minSeconds || seconds > maxSeconds) Left(Err.NumberOutOfRange(seconds))
      else Right(new Timeout(seconds.toInt))

    def unsafe(seconds: Long): Timeout = safe(seconds).toTry.get
  }

  /** The Number, Sip and Client nouns that one Dial verb calls.
    *
    * Twilio allows `minCount` to `maxCount` nouns in one Dial verb, also of different types.
    */
  final class Nouns private (val nouns: immutable.Seq[DestinationNoun])

  object Nouns {

    val minCount: Int = 1

    val maxCount: Int = 10

    sealed trait Err extends RuntimeException
    object Err {
      final case class CountOutOfRange(count: Int)
          extends IllegalArgumentException(
            s"A Dial verb needs $minCount to $maxCount nouns, not $count."
          )
          with Err
    }

    /** Build the nouns of a Dial verb from 1 to 10 nouns, when the number of nouns is known as the
      * code is written.
      *
      * There is one apply method for each number of nouns, so the compiler checks the count. For a
      * list of nouns, use [[fromSeq]] or [[fromSeqUnsafe]].
      */
    def apply(noun1: DestinationNoun): Nouns = new Nouns(List(noun1))

    def apply(noun1: DestinationNoun, noun2: DestinationNoun): Nouns =
      new Nouns(List(noun1, noun2))

    def apply(noun1: DestinationNoun, noun2: DestinationNoun, noun3: DestinationNoun): Nouns =
      new Nouns(List(noun1, noun2, noun3))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4, noun5))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun,
        noun6: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4, noun5, noun6))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun,
        noun6: DestinationNoun,
        noun7: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4, noun5, noun6, noun7))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun,
        noun6: DestinationNoun,
        noun7: DestinationNoun,
        noun8: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4, noun5, noun6, noun7, noun8))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun,
        noun6: DestinationNoun,
        noun7: DestinationNoun,
        noun8: DestinationNoun,
        noun9: DestinationNoun
    ): Nouns = new Nouns(List(noun1, noun2, noun3, noun4, noun5, noun6, noun7, noun8, noun9))

    def apply(
        noun1: DestinationNoun,
        noun2: DestinationNoun,
        noun3: DestinationNoun,
        noun4: DestinationNoun,
        noun5: DestinationNoun,
        noun6: DestinationNoun,
        noun7: DestinationNoun,
        noun8: DestinationNoun,
        noun9: DestinationNoun,
        noun10: DestinationNoun
    ): Nouns =
      new Nouns(List(noun1, noun2, noun3, noun4, noun5, noun6, noun7, noun8, noun9, noun10))

    def fromSeq(nouns: immutable.Seq[DestinationNoun]): Either[Err, Nouns] =
      if (nouns.size < minCount || nouns.size > maxCount) Left(Err.CountOutOfRange(nouns.size))
      else Right(new Nouns(nouns))

    def fromSeqUnsafe(nouns: immutable.Seq[DestinationNoun]): Nouns = fromSeq(nouns).toTry.get
  }

  final class Builder[
      B <: Buildable,
      S <: HasSingleAllowedValueAlready,
      R <: PhantomTypes.RecordSet,
      C <: PhantomTypes.RecordingStatusCallbackSet
  ] private (
      value: ValueToUse,
      callerId: Option[String],
      record: Option[Record],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackMethod: Option[HttpMethod],
      action: Option[CallbackUrl],
      timeout: Option[Timeout],
      sequential: Option[Boolean]
  ) {

    private def copy[
        BNew <: Buildable,
        SNew <: HasSingleAllowedValueAlready,
        RNew <: PhantomTypes.RecordSet,
        CNew <: PhantomTypes.RecordingStatusCallbackSet
    ](
        value: ValueToUse = this.value,
        callerId: Option[String] = this.callerId,
        record: Option[Record] = this.record,
        recordingStatusCallback: Option[CallbackUrl] = this.recordingStatusCallback,
        recordingStatusCallbackMethod: Option[HttpMethod] = this.recordingStatusCallbackMethod,
        action: Option[CallbackUrl] = this.action,
        timeout: Option[Timeout] = this.timeout,
        sequential: Option[Boolean] = this.sequential
    ): Builder[BNew, SNew, RNew, CNew] =
      new Builder(
        value,
        callerId,
        record,
        recordingStatusCallback,
        recordingStatusCallbackMethod,
        action,
        timeout,
        sequential
      )

    private type BuilderWithSameTypes = Builder[B, S, R, C]

    def withPhoneNumber(pn: PhoneNumberE164)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue, R, C] =
      copy(value = ValuePhoneNumber(pn))

    def withConference(fun: ConferenceNoun.BuildFunction)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue, R, C] =
      copy(value = ValueConference(ConferenceNoun.build(fun)))

    /** Dial the Number, Sip and Client nouns of the given [[Nouns]].
      *
      * Twilio calls all of them at the same time, and connects the first one that answers. With
      * [[withSequential]] set to true, Twilio calls them one after the other instead.
      *
      * A Conference noun cannot be one of these nouns, and is set with [[withConference]] instead.
      * Twilio lists only the Number, Sip and Client nouns as nouns that can be mixed in one Dial
      * verb.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/sip
      */
    def withNouns(nouns: Nouns)(
        implicit evS: S =:= HasSingleAllowedValueAlreadyFalse
    ): Builder[BuildableTrue, HasSingleAllowedValueAlreadyTrue, R, C] =
      copy(value = ValueNouns(nouns))

    /** Set a phone number as the caller ID that the dialed party sees.
      *
      * Twilio accepts the To or From number of the call, a phone number bought from Twilio, or a
      * verified caller ID. Without a caller ID, the dialed party sees the caller ID of the call
      * that runs this TwiML.
      */
    def withCallerId(phoneNumber: PhoneNumberE164): BuilderWithSameTypes =
      copy(callerId = Some(phoneNumber.asString))

    /** Set a client identifier as the caller ID, for a Dial to a Client noun.
      *
      * Twilio receives it as `client:<identity>`.
      */
    def withCallerId(identity: ClientIdentity): BuilderWithSameTypes =
      copy(callerId = Some(s"client:${identity.twilioString}"))

    /** Set a text as the caller ID, for a Dial to a Sip noun. See
      * [[com.dixa.twilio.model.voice.AlphanumericCallerId]].
      */
    def withCallerId(callerId: AlphanumericCallerId): BuilderWithSameTypes =
      copy(callerId = Some(callerId.twilioString))

    /** Record the dialed call.
      *
      * Twilio does not record the dialed call when this is not set.
      */
    def withRecord(record: Record): Builder[B, S, PhantomTypes.RecordSetTrue, C] =
      copy(record = Some(record))

    /** Set the URL that Twilio requests when the recording is available.
      *
      * Can only be called if [[withRecord]] has already been called.
      */
    def withRecordingStatusCallback(callback: CallbackUrl)(
        implicit evR: R =:= PhantomTypes.RecordSetTrue
    ): Builder[B, S, R, PhantomTypes.RecordingStatusCallbackSetTrue] =
      copy(recordingStatusCallback = Some(callback))

    /** Set the HTTP method that Twilio uses to request the recordingStatusCallback URL.
      *
      * Twilio uses POST when this is not set. Can only be called if [[withRecordingStatusCallback]]
      * has already been called.
      */
    def withRecordingStatusCallbackMethod(method: HttpMethod)(
        implicit evC: C =:= PhantomTypes.RecordingStatusCallbackSetTrue
    ): BuilderWithSameTypes =
      copy(recordingStatusCallbackMethod = Some(method))

    /** Set the URL that Twilio requests when the Dial ends.
      *
      * Twilio continues the call with the TwiML that the URL returns, and ignores the verbs after
      * the Dial. Without an action, Twilio continues with the next verb after the Dial.
      */
    def withAction(action: CallbackUrl): BuilderWithSameTypes = copy(action = Some(action))

    /** Set how long Twilio lets the dialed party ring. See [[Timeout]]. */
    def withTimeout(timeout: Timeout): BuilderWithSameTypes = copy(timeout = Some(timeout))

    /** With true, Twilio calls the nouns of [[withNouns]] one after the other, instead of all at
      * the same time.
      *
      * Twilio uses false when this is not set.
      */
    def withSequential(sequential: Boolean): BuilderWithSameTypes =
      copy(sequential = Some(sequential))

    def build()(
        implicit evB: B =:= BuildableTrue
    ): DialVerb = DialVerbImpl(
      value,
      callerId,
      record,
      recordingStatusCallback,
      recordingStatusCallbackMethod,
      action,
      timeout,
      sequential
    )
  }

  object Builder {
    val empty: BuilderStartState =
      new BuilderStartState(NotSetValue, None, None, None, None, None, None, None)
  }

  type BuilderStartState = Builder[
    BuildableFalse,
    HasSingleAllowedValueAlreadyFalse,
    PhantomTypes.RecordSetFalse,
    PhantomTypes.RecordingStatusCallbackSetFalse
  ]
  type BuildFunction = BuilderStartState => DialVerb

  def build(fun: BuildFunction): DialVerb = fun(Builder.empty)

  private sealed abstract class ValueToUse
  private object NotSetValue                                           extends ValueToUse
  private final case class ValuePhoneNumber(pn: PhoneNumberE164)       extends ValueToUse
  private final case class ValueConference(conference: ConferenceNoun) extends ValueToUse
  private final case class ValueNouns(nouns: Nouns)                    extends ValueToUse

  private final case class DialVerbImpl(
      value: ValueToUse,
      callerId: Option[String],
      record: Option[Record],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackMethod: Option[HttpMethod],
      action: Option[CallbackUrl],
      timeout: Option[Timeout],
      sequential: Option[Boolean]
  ) extends DialVerb {

    override protected def tagAttributes: immutable.Seq[(String, String)] =
      new TagAttributeBuilder()
        .addString("callerId", callerId)
        .add("record", record)
        .add("recordingStatusCallbackMethod", recordingStatusCallbackMethod)
        .add("recordingStatusCallback", recordingStatusCallback)
        .add("action", action)
        .add("timeout", timeout)
        .addBoolean("sequential", sequential)
        .build

    override protected def tagSubElements: immutable.Seq[TwimlElement] = value match {
      case ValueConference(conference) => List(conference)
      case ValueNouns(dialNouns)       => dialNouns.nouns
      case _                           => Nil
    }

    override protected def tagValue: Option[String] = value match {
      case ValuePhoneNumber(pn) => Some(pn.asString)
      case _                    => None
    }
  }

}
