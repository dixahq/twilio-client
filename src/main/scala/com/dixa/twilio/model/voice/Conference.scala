package com.dixa.twilio.model.voice

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.{
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
      edgeLocation: PublicEdgeLocation,
      reasonConferenceEnded: Option[EndReason],
      callSidEndingConference: Option[Call.Sid],
      participants: Vector[Participant]
  ) extends Conference

  final case class Sid private[Conference] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("CF")), new Sid(_))

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
      case object Queued     extends Status("queued", isActive = true)
      case object Connecting extends Status("connecting", isActive = true)
      case object Ringing    extends Status("ringing", isActive = true)
      case object Connected  extends Status("connected", isActive = true)
      case object Complete   extends Status("complete", isActive = false)
      case object Failed     extends Status("failed", isActive = false)
    }

    /** The jitterBufferSize attribute lets you set the jitter buffer behavior for a conference
      * participant.
      *
      * Twilio Conference uses a jitter buffer to smooth out irregularity in media packet arrival
      * times when mixing audio for conference participants. This buffer results in fewer audio
      * artifacts, but introduces a fixed delay for the audio of each participant.
      *
      * Setting the jitterBufferSize value to small will create a 20ms buffer that results in
      * average latency of ~150ms - ~200ms on a stream with max jitter of ~20ms.
      *
      * Setting the value to medium will create a 40ms buffer that results in average latency of
      * ~200ms - ~360ms on a stream with max jitter of ~20ms.
      *
      * The large setting, which is the default jitter buffer behavior, will create a 60ms buffer
      * that results in average latency between ~300ms - ~1000ms on a stream with max jitter of
      * ~20ms.
      *
      * Spikes of extremely high jitter can result in the maximum latency exceeding the average
      * latency by as much as 50%.
      *
      * The off setting completely disables the buffer and packets with relatively low jitter (
      * <=20ms) will be completely dropped, but Twilio will add no extra latency when mixing.
      *
      * The buffer value is a particpant-level setting, the value for participant A does not apply
      * to participant B.
      *
      * @see
      *   https://www.twilio.com/docs/voice/twiml/conference#attributes-jitterBufferSize
      */
    sealed abstract class JitterBufferSize(override val toString: String)
        extends EnumWithTwilioString.EnumEntry

    object JitterBufferSize extends EnumWithTwilioString[JitterBufferSize] {
      override def values: scala.collection.immutable.IndexedSeq[JitterBufferSize] = findValues

      case object Small  extends JitterBufferSize("small")
      case object Medium extends JitterBufferSize("medium")
      case object Large  extends JitterBufferSize("large")
      case object Off    extends JitterBufferSize("off")
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

  final case class MaxParticipants private (toInt: Int) extends TwilioStringValue {
    override def toString: String = toInt.toString
  }

  object MaxParticipants {

    sealed trait Err extends RuntimeException
    object Err {
      final case class NumberOutOfRange(i: Int)
          extends IllegalArgumentException(
            s"$i is not between 1 and 250 as required for the max participants setting."
          )
          with Err
    }

    def safe(fromInt: Int): Either[Err, MaxParticipants] = if (fromInt < 1 || fromInt > 250)
      Left(Err.NumberOutOfRange(fromInt))
    else Right(new MaxParticipants(fromInt))

    def unsafe(fromInt: Int): MaxParticipants = safe(fromInt).toTry.get
  }

  sealed abstract class Record(override val toString: String) extends EnumWithTwilioString.EnumEntry
  object Record extends EnumWithTwilioString[Record] {
    override def values: scala.collection.immutable.IndexedSeq[Record] = findValues

    case object DoNotRecord     extends Record("do-not-record")
    case object RecordFromStart extends Record("record-from-start")
  }

  sealed abstract class Trim(override val toString: String) extends EnumWithTwilioString.EnumEntry
  object Trim extends EnumWithTwilioString[Trim] {
    override def values: scala.collection.immutable.IndexedSeq[Trim] = findValues

    case object TrimSilence extends Trim("trim-silence")
    case object DoNotTrim   extends Trim("do-not-trim")
  }

  /** The statusCallbackEvent attribute allows you to specify which conference state changes should
    * generate a Webhook to the URL specified in the statusCallback attribute. The available values
    * are start, end, join, leave, mute, hold, modify, speaker, and announcement. To specify
    * multiple values separate them with a space. Events are set by the first Participant to join
    * the conference, subsequent statusCallbackEvents will be ignored. If you specify conference
    * events you can see a log of the events fired for a given conference in the conference logs in
    * the console.
    *
    * @param documentationOrder
    *   represent the order that the twilio documentation refers to each value in.
    * @see
    *   https://www.twilio.com/docs/voice/twiml/conference#attributes-statusCallbackEvent
    */
  sealed abstract class StatusCallbackEvent(
      override val toString: String,
      val documentationOrder: Int
  ) extends EnumWithTwilioString.EnumEntry
  object StatusCallbackEvent extends EnumWithTwilioString[StatusCallbackEvent] {
    override def values: scala.collection.immutable.IndexedSeq[StatusCallbackEvent] = findValues

    /** The conference has begun and audio is being mixed between all participants. This occurs when
      * there are at least two participants in the conference, and at least one of the participants
      * has startConferenceOnEnter="true".
      */
    case object Start extends StatusCallbackEvent("start", 1)

    /** The last participant has left the conference or a participant with
      * endConferenceOnExit="true" leaves the conference.
      */
    case object End extends StatusCallbackEvent("end", 2)

    /** A participant has joined the conference. */
    case object Join extends StatusCallbackEvent("join", 3)

    /** A participant has left the conference. */
    case object Leave extends StatusCallbackEvent("leave", 4)

    /** A participant has been muted or unmuted. */
    case object Mute extends StatusCallbackEvent("mute", 5)

    /** A participant has been held or unheld. */
    case object Hold extends StatusCallbackEvent("hold", 6)

    /** At least one of a participant's attributes has been modified: BeepOnExit,
      * EndConferenceOnExit, Coaching, WaitUrl
      */
    case object Modify extends StatusCallbackEvent("modify", 7)

    /** A participant has started or stopped speaking. */
    case object Speaker extends StatusCallbackEvent("speaker", 8)

    /** A participant or conference announcement has ended or failed. Currently, the
      * announcement-fail event will only be sent if there is an internal Twilio error. We are
      * working to add more failures to the announcement-fail event to allow developers to debug the
      * issue.
      */
    case object Announcement extends StatusCallbackEvent("announcement", 9)
  }

  /** This attribute allows you to specify which recording status changes should generate a webhook
    * to the URL specified in the recordingStatusCallback attribute. The available values are
    * in-progress, completed, absent. To specify multiple values separate them with a space. Default
    * is completed.
    *
    * @param documentationOrder
    *   represent the order that the twilio documentation refers to each value in.
    * @see
    *   https://www.twilio.com/docs/voice/twiml/conference#attributes-recording-status-callback-event
    */
  sealed abstract class RecordingStatusCallbackEvent(
      override val toString: String,
      val documentationOrder: Int
  ) extends EnumWithTwilioString.EnumEntry
  object RecordingStatusCallbackEvent extends EnumWithTwilioString[RecordingStatusCallbackEvent] {
    override def values: scala.collection.immutable.IndexedSeq[RecordingStatusCallbackEvent] =
      findValues

    /** The recording has started */
    case object InProgress extends RecordingStatusCallbackEvent("in-progress", 1)

    /** The recording is complete and available for access */
    case object Completed extends RecordingStatusCallbackEvent("completed", 2)

    /** The recording is absent and not accessible */
    case object Absent extends RecordingStatusCallbackEvent("absent", 3)
  }

}
