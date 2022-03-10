package com.dixa.twilio.model.voice

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.iam.TwilioAccount

import scala.collection.immutable

sealed trait TwilioConference {
  def sid: TwilioConference.Sid
  def status: TwilioConference.Status
  def friendlyName: TwilioConference.FriendlyName
  def accountSid: TwilioAccount.Sid
}

object TwilioConference {

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
  ) extends TwilioConference

  final case class TwilioConferenceWithParticipants(
      sid: Sid,
      status: Status,
      friendlyName: FriendlyName,
      accountSid: TwilioAccount.Sid,
      participants: Vector[Participant]
  ) extends TwilioConference

  final case class Sid(override val toString: String)

  sealed abstract class Status(
      val twilioString: String,
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

  final case class FriendlyName(override val toString: String)

  sealed abstract class ParticipantStatus(
      val twilioString: String,
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

  final case class Participant(callSid: TwilioCallSid, status: ParticipantStatus)

}
