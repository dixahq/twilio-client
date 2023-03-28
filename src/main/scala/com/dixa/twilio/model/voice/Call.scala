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

import java.time.Instant
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
    duration: Option[Call.Duration],
    price: Option[Call.Price],
    direction: Call.Direction,
    answeredBy: Option[Call.AnsweredBy],
    forwardedFrom: Option[Call.ForwardedFrom],
    groupSid: Option[Group.Sid],
    callerName: Option[Call.Name],
    queueTime: Call.QueueTime,
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

  object Sid extends SidCompanionObject(Prefix("CA"), new Sid(_))

  final case class CallerId(override val toString: String) extends TwilioStringValue {
    def toPhoneNumber: Option[PhoneNumberE164] = {
      PhoneNumberE164(toString)
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
    override val toString: String = duration.toString
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

  final case class Duration(override val toString: String) extends TwilioStringValue

  final case class Price(amount: BigDecimal, unit: Iso4127CountryCode) extends TwilioStringValue {
    override def toString: String = s"$amount $unit"
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

    case object Human   extends Direction("human")
    case object Machine extends Direction("machine")
  }

  final case class ForwardedFrom(override val toString: String) extends TwilioStringValue

  final case class Name(override val toString: String) extends TwilioStringValue

  final case class QueueTime(override val toString: String) extends TwilioStringValue

  final case class FormattedPhoneNumber(override val toString: String) extends TwilioStringValue
}
