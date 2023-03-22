package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{
  ApiVersion,
  EnumWithTwilioString,
  PublicEdgeLocation,
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
  def edgeLocation: PublicEdgeLocation
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
      edgeLocation: PublicEdgeLocation,
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
    edgeLocation,
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
      edgeLocation: PublicEdgeLocation,
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
      edgeLocation: PublicEdgeLocation,
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

  final case class Participant(
      accountSid: TwilioAccount.Sid,
      callSid: Call.Sid,
      label: Option[Participant.Label],
      callSidToCoach: Option[Call.Sid],
      coaching: Boolean,
      conferenceSid: Conference.Sid,
      dateCreated: Instant,
      dateUpdated: Instant,
      endConferenceOnExit: Boolean,
      muted: Boolean,
      hold: Boolean,
      startConferenceOnEnter: Boolean,
      status: Participant.Status,
  )

  object Participant {

    final case class Label(override val toString: String) extends TwilioStringValue

    sealed abstract class Status(
        override val twilioString: String,

        /** Specifies if this status is one, where the participant are considered active
          *
          * By active means a state where the participant is either activily part of the conference,
          * or is expected to be it in the future. So status like queued and connecting is also
          * considered active.
          */
        val isActive: Boolean
    ) extends EnumWithTwilioString.EnumEntry

    object Status extends EnumWithTwilioString[Status] {
      override val values: immutable.IndexedSeq[Status] = findValues
      case object Queued extends Status("queued", isActive = true)
      case object Connecting extends Status("connecting", isActive = true)
      case object Ringing extends Status("ringing", isActive = true)
      case object Connected extends Status("connected", isActive = true)
      case object Complete extends Status("complete", isActive = false)
      case object Failed extends Status("failed", isActive = false)
    }
  }

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
