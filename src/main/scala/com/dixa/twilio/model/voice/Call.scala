package com.dixa.twilio.model.voice

import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract}
import com.dixa.twilio.model.SidAbstract.{Prefix, SidCompanionObject}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.collection.immutable

final case class Call(
    sid: Call.Sid,
    accountSid: TwilioAccount.Sid

    // A lot of attributes are missing here, but did not need them at time of writing,
    // so add them later once needed.
    // Beware though, that to and from cannot just be phone numbers, as they
    // are often also sip addresses. So some kind of abstraction over that would be needed.
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

  sealed abstract class StatusUpdate(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object StatusUpdate extends EnumWithTwilioString[StatusUpdate] {
    override val values: immutable.IndexedSeq[StatusUpdate] = findValues

    case object Init extends StatusUpdate("init")

    case object InProgress extends StatusUpdate("in-progress")

    case object Completed extends StatusUpdate("completed")
  }

  final case class TimeLimit(duration: Int)
}
