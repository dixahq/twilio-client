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

import com.dixa.twilio.model.{
  EnumWithTwilioString,
  Iso4127CountryCode,
  SidAbstract,
  TwilioStringValue
}
import com.dixa.twilio.model.SidAbstract.{Prefix, SidCompanionObject}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, TwilioPhoneNumber}

import java.time.{Duration, Instant}
import scala.annotation.nowarn
import scala.collection.immutable

final case class Call(
    sid: Call.Sid,
    dateCreated: Instant,
    dateUpdate: Instant,
    parentCallSid: Option[Call.Sid],
    accountSid: TwilioAccount.Sid,
    to: Call.CallerId,
    toFormatted: Call.FormattedPhoneNumber,
    from: Call.CallerId,
    fromFormatted: Call.FormattedPhoneNumber,
    phoneNumberSid: Option[TwilioPhoneNumber.Sid],
    status: Call.Status,
    startTime: Option[Instant],
    endTime: Option[Instant],
    duration: Option[Duration],
    price: Option[Call.Price],
    direction: Call.Direction,
    answeredBy: Option[Call.AnsweredBy],
    forwardedFrom: Option[Call.ForwardedFrom],
    groupSid: Option[Group.Sid],
    callerName: Option[Call.Name],
    queueTime: Duration,
    trunkSid: Option[Trunk.Sid],
)

object Call {

  /** Represent a Twilio Call SID
    *
    * Input must apply to the format that Twilio specify as a Call SID: "It is a 34 character string
    * that starts with CA"
    *
    * The twilio documentation about it can be found here:
    * https://support.twilio.com/hc/en-us/articles/223180488-What-is-a-Call-SID-
    */
  final case class Sid private[Call] (override val toString: String) extends SidAbstract

  object Sid extends SidCompanionObject(List(Prefix("CA")), new Sid(_))

  final case class CallerId(override val twilioString: String) extends TwilioStringValue {
    def toPhoneNumber: Option[PhoneNumberE164] = {
      PhoneNumberE164(twilioString)
    }
  }

  sealed abstract class StatusUpdate(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object StatusUpdate extends EnumWithTwilioString[StatusUpdate] {
    override val values: immutable.IndexedSeq[StatusUpdate] = findValues

    case object Init       extends StatusUpdate("init")
    case object InProgress extends StatusUpdate("in-progress")
    case object Completed  extends StatusUpdate("completed")
  }

  final case class TimeLimit(duration: Int) extends TwilioStringValue {
    override val twilioString: String = duration.toString
  }

  sealed abstract class Status(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Queued     extends Status("queued")
    case object Ringing    extends Status("ringing")
    case object InProgress extends Status("in-progress")
    case object Canceled   extends Status("canceled")
    case object Completed  extends Status("completed")
    case object Failed     extends Status("failed")
    case object Busy       extends Status("busy")
    case object NoAnswer   extends Status("no-answer")
  }

  final case class Price(amount: BigDecimal, unit: Iso4127CountryCode) extends TwilioStringValue {
    override def twilioString: String = s"$amount $unit"
  }

  sealed abstract class Direction(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Direction extends EnumWithTwilioString[Direction] {
    override val values: immutable.IndexedSeq[Direction] = findValues

    case object Inbound             extends Direction("inbound")
    case object OutboundApi         extends Direction("outbound-api")
    case object OutboundDial        extends Direction("outbound-dial")
    case object TrunkingTerminating extends Direction("trunking-terminating")
    case object TrunkingOriginating extends Direction("trunking-originating")
  }

  sealed abstract class AnsweredBy(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object AnsweredBy extends EnumWithTwilioString[AnsweredBy] {
    override val values: immutable.IndexedSeq[AnsweredBy] = findValues

    case object Human   extends AnsweredBy("human")
    case object Machine extends AnsweredBy("machine")
  }

  final case class ForwardedFrom(override val twilioString: String) extends TwilioStringValue

  final case class Name(override val twilioString: String) extends TwilioStringValue

  final case class FormattedPhoneNumber(override val twilioString: String) extends TwilioStringValue

  sealed abstract class RecordingChannels(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object RecordingChannels extends EnumWithTwilioString[RecordingChannels] {
    override val values: immutable.IndexedSeq[RecordingChannels] = findValues

    case object Mono extends RecordingChannels("mono")

    case object Dual extends RecordingChannels("dual")

  }

  sealed abstract class MachineDetection(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object MachineDetection extends EnumWithTwilioString[RecordingChannels] {
    override val values: immutable.IndexedSeq[RecordingChannels] = findValues

    case object Enable extends MachineDetection("enable")

    case object DetectMessageEnd extends MachineDetection("detect-message-end")

  }

  sealed abstract class ProgressEvent(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object ProgressEvent extends EnumWithTwilioString[ProgressEvent] {
    override val values: immutable.IndexedSeq[ProgressEvent] = findValues

    case object Initiated extends ProgressEvent("initiated")

    case object Ringing extends ProgressEvent("ringing")

    case object Answered extends ProgressEvent("answered")

    case object Completed extends ProgressEvent("completed")
  }

  sealed abstract class RecordingEvent(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object RecordingEvent extends EnumWithTwilioString[RecordingEvent] {
    override val values: immutable.IndexedSeq[RecordingEvent] = findValues

    case object InProgress extends RecordingEvent("in-progress")

    case object Completed extends RecordingEvent("completed")

    case object Absent extends RecordingEvent("absent")
  }

  sealed abstract class Trim(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object Trim extends EnumWithTwilioString[Trim] {
    override val values: immutable.IndexedSeq[Trim] = findValues

    case object TrimSilence extends Trim("trim-silence")

    case object DoNotTrim extends Trim("do-not-trim")
  }

  final case class Reason(override val twilioString: String) extends TwilioStringValue

  final case class Token(override val twilioString: String) extends TwilioStringValue

  sealed abstract class RecordingTrack(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object RecordingTrack extends EnumWithTwilioString[RecordingTrack] {
    override val values: immutable.IndexedSeq[RecordingTrack] = findValues

    case object Inbound extends RecordingTrack("inbound")

    case object Outbound extends RecordingTrack("outbound")

    case object Both extends RecordingTrack("both")
  }

  /** Timeout can only be positive and not larger than 600 seconds. Default is 60 seconds. For some
    * call flows Twilio will add extra 5 seconds.
    */
  final case class Timeout private (int: Int) extends TwilioStringValue {
    override def twilioString: String = int.toString
  }

  object Timeout {

    sealed trait Err extends RuntimeException

    object Err {
      case class NotPositive(int: Int)
          extends RuntimeException(s"$int is not a positive integer ( > 0 )")
          with Err

      case class MaximumReached(int: Int)
          extends RuntimeException(s"Timeout cannot be more than 600 seconds")
          with Err
    }

    // override apply method as private, to ensure clients cannot create invalid instance.
    @nowarn(value = "cat=unused")
    private def apply(int: Int): Timeout = new Timeout(int)

    def safe(int: Int): Either[Err, Timeout] = {
      if (int < 0) Left(Err.NotPositive(int))
      else if (int > 600) Left(Err.MaximumReached(int))
      else Right(new Timeout(int))
    }

    def unsafe(int: Int): Timeout = safe(int).toTry.get
  }

  /** Default threshold: 2400
    */
  final case class MachineDetectionSpeechThreshold private (int: Int) extends TwilioStringValue {
    override def twilioString: String = int.toString
  }

  object MachineDetectionSpeechThreshold {

    sealed trait Err extends RuntimeException

    object Err {
      case class ValueBelowAllowed(int: Int)
          extends RuntimeException("Machine detection speech threshold cannot be less than 1000")
          with Err

      case class ValueAboveAllowed(int: Int)
          extends RuntimeException("Machine detection speech threshold cannot be more than 6000")
          with Err
    }

    // override apply method as private, to ensure clients cannot create invalid instance.
    @nowarn(value = "cat=unused")
    private def apply(int: Int): MachineDetectionSpeechThreshold =
      new MachineDetectionSpeechThreshold(int)

    def safe(int: Int): Either[Err, MachineDetectionSpeechThreshold] = {
      if (int < 1000) Left(Err.ValueBelowAllowed(int))
      else if (int > 6000) Left(Err.ValueAboveAllowed(int))
      else Right(new MachineDetectionSpeechThreshold(int))
    }

    def unsafe(int: Int): MachineDetectionSpeechThreshold = safe(int).toTry.get
  }

  /** Default threshold: 1200
    */
  final case class MachineDetectionSpeechEndThreshold private (int: Int) extends TwilioStringValue {
    override def twilioString: String = int.toString
  }

  object MachineDetectionSpeechEndThreshold {

    sealed trait Err extends RuntimeException

    object Err {
      case class ValueBelowAllowed(int: Int)
          extends RuntimeException("Machine detection speech end threshold cannot be less than 500")
          with Err

      case class ValueAboveAllowed(int: Int)
          extends RuntimeException(
            "Machine detection speech end threshold cannot be more than 5000"
          )
          with Err
    }

    // override apply method as private, to ensure clients cannot create invalid instance.
    @nowarn(value = "cat=unused")
    private def apply(int: Int): MachineDetectionSpeechEndThreshold =
      new MachineDetectionSpeechEndThreshold(int)

    def safe(int: Int): Either[Err, MachineDetectionSpeechEndThreshold] = {
      if (int < 500) Left(Err.ValueBelowAllowed(int))
      else if (int > 5000) Left(Err.ValueAboveAllowed(int))
      else Right(new MachineDetectionSpeechEndThreshold(int))
    }

    def unsafe(int: Int): MachineDetectionSpeechEndThreshold = safe(int).toTry.get
  }

  /** Default threshold: 5000
    */
  final case class MachineDetectionSilenceTimeout private (int: Int) extends TwilioStringValue {
    override def twilioString: String = int.toString
  }

  object MachineDetectionSilenceTimeout {

    sealed trait Err extends RuntimeException

    object Err {
      case class ValueBelowAllowed(int: Int)
          extends RuntimeException("Machine detection silence timeout cannot be less than 2000")
          with Err

      case class ValueAboveAllowed(int: Int)
          extends RuntimeException("Machine detection silence timeout cannot be more than 10000")
          with Err
    }

    // override apply method as private, to ensure clients cannot create invalid instance.
    @nowarn(value = "cat=unused")
    private def apply(int: Int): MachineDetectionSilenceTimeout =
      new MachineDetectionSilenceTimeout(int)

    def safe(int: Int): Either[Err, MachineDetectionSilenceTimeout] = {
      if (int < 2000) Left(Err.ValueBelowAllowed(int))
      else if (int > 10000) Left(Err.ValueAboveAllowed(int))
      else Right(new MachineDetectionSilenceTimeout(int))
    }

    def unsafe(int: Int): MachineDetectionSilenceTimeout = safe(int).toTry.get
  }

}
