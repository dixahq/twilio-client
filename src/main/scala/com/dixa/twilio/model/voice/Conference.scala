package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{
  ApiVersion,
  EnumWithTwilioString,
  Region,
  SidAbstract,
  TwilioStringValue
}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.voice.Conference.EndReason

import java.time.Instant
import scala.collection.immutable

sealed trait Conference {
  def sid: Conference.Sid
  def status: Conference.Status
  def friendlyName: Conference.FriendlyName
  def accountSid: TwilioAccount.Sid
  def dateCreated: Instant
  def dateUpdated: Instant
  def apiVersion: ApiVersion
  def region: Region
  def reasonConferenceEnded: Option[EndReason]
  def callSidEndingConference: Option[Call.Sid]
}

object Conference {

  def apply(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
      dateCreated: Instant,
      dateUpdated: Instant,
      apiVersion: ApiVersion,
      region: Region,
      reasonConferenceEnded: Option[EndReason],
      callSidEndingConference: Option[Call.Sid]
  ): DefaultImpl = DefaultImpl(
    sid,
    status,
    friendlyName,
    accountSid,
    dateCreated,
    dateUpdated,
    apiVersion,
    region,
    reasonConferenceEnded,
    callSidEndingConference
  )

  final case class DefaultImpl(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
      dateCreated: Instant,
      dateUpdated: Instant,
      apiVersion: ApiVersion,
      region: Region,
      reasonConferenceEnded: Option[EndReason],
      callSidEndingConference: Option[Call.Sid]
  ) extends Conference

  final case class ConferenceWithParticipants(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
      dateCreated: Instant,
      dateUpdated: Instant,
      apiVersion: ApiVersion,
      region: Region,
      reasonConferenceEnded: Option[EndReason],
      callSidEndingConference: Option[Call.Sid],
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

  sealed abstract class EndReason(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object EndReason extends EnumWithTwilioString[EndReason] {
    override def values: immutable.IndexedSeq[EndReason] = findValues

    case object ConferenceEndedViaApi extends EndReason("conference-ended-via-api")
    case object ParticipantWithEndConferenceOnExitLeft
        extends EndReason("participant-with-end-conference-on-exit-left")
    case object ParticipantWithEndConferenceOnExitKicked
        extends EndReason("participant-with-end-conference-on-exit-kicked")
    case object LastParticipantKicked extends EndReason("last-participant-kicked")
    case object LastParticipantLeft   extends EndReason("last-participant-left")
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
