package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract, TwilioStringValue}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.collection.immutable

sealed trait Conference {
  def sid: Conference.Sid
  def status: Conference.Status
  def friendlyName: Conference.FriendlyName
  def accountSid: TwilioAccount.Sid
}

object Conference {

  def apply(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
  ): DefaultImpl = DefaultImpl(sid, status, friendlyName, accountSid)

  final case class DefaultImpl(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
  ) extends Conference

  final case class ConferenceWithParticipants(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
      participants: Vector[Participant]
  ) extends Conference

  final case class Sid private[Conference] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(Prefix("CF"), new Sid(_))

  sealed abstract class Status(
      override val twilioString: String,
      /** Specifies if this conference status is considerd active
        *
        * By active is meant a status where it is in progress or will end up in-progress in the
        * future.
        */
      val isActive: Boolean
  ) extends EnumWithTwilioString.EnumEntry
  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Init       extends Status("init", isActive = true)
    case object InProgress extends Status("in-progress", isActive = true)
    case object Completed  extends Status("completed", isActive = false)
  }

  final case class FriendlyName(override val toString: String) extends TwilioStringValue

  sealed abstract class ParticipantStatus(
      override val twilioString: String,
      /** Specifies if this status is one, where the participant are considered active
        *
        * By active means a state where the participant is either activily part of the conference,
        * or is expected to be it in the future. So status like queued and connecting is also
        * considered active.
        */
      val isActive: Boolean
  ) extends EnumWithTwilioString.EnumEntry
  object ParticipantStatus extends EnumWithTwilioString[ParticipantStatus] {
    override val values: immutable.IndexedSeq[ParticipantStatus] = findValues

    case object Queued     extends ParticipantStatus("queued", isActive = true)
    case object Connecting extends ParticipantStatus("connecting", isActive = true)
    case object Ringing    extends ParticipantStatus("ringing", isActive = true)
    case object Connected  extends ParticipantStatus("connected", isActive = true)
    case object Complete   extends ParticipantStatus("complete", isActive = false)
    case object Failed     extends ParticipantStatus("failed", isActive = false)
  }

  final case class Participant(callSid: Call.Sid, status: ParticipantStatus)

  /** Represent the Beep attribute of an conference.
    *
    *   - true = Plays a beep both when a participant joins and when a participant leaves.
    *   - false = Disables beeps for when participants both join and exit.
    *   - onEnter = Only plays a beep when a participant joins. The beep will not be played when the
    *     participant exits.
    *   - onExit = Will not play a beep when a participant joins; only plays a beep when the
    *     participant exits.
    *
    * This attribute is set when creating a conference via TwiML dial verb:
    * [[https://www.twilio.com/docs/voice/twiml/conference#attributes-beep]]
    */
  sealed abstract class Beep(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object Beep extends EnumWithTwilioString[Beep] {
    case object True    extends Beep("true")
    case object False   extends Beep("false")
    case object OnEnter extends Beep("onEnter")
    case object OnExit  extends Beep("onExit")

    override def values: immutable.IndexedSeq[Beep] = findValues
  }
}
