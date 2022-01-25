package com.dixa.twilio.client.model

import enumeratum.{Enum, EnumEntry}

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
      private[client] val twilioApiStringRep: String,
      /** Specifies if this conference status is considerd active
        *
        * By active is meant a status where it is in progress or will end up in-progress in the
        * future.
        */
      val isActive: Boolean
  ) extends EnumEntry
  object Status extends Enum[Status] {
    override def values: immutable.IndexedSeq[Status] = findValues

    case object Init       extends Status("init", isActive = true)
    case object InProgress extends Status("in-progress", isActive = true)
    case object Completed  extends Status("completed", isActive = false)

    private[client] def fromTwilioStringStatus(s: String): Status = s match {
      case Init.twilioApiStringRep       => Init
      case InProgress.twilioApiStringRep => InProgress
      case Completed.twilioApiStringRep  => Completed
    }
  }

  final case class FriendlyName(override val toString: String)

  sealed abstract class ParticipantStatus(
      private[client] val twilioApiStringRep: String,
      /** Specifies if this status is one, where the participant are considere active
        *
        * By active means a state where the participant is either activily part of the conference,
        * or is expected to be it in the future. So status like queued and connecting is also
        * considered active.
        */
      val isActive: Boolean
  ) extends EnumEntry
  object ParticipantStatus extends Enum[ParticipantStatus] {
    override def values: immutable.IndexedSeq[ParticipantStatus] = findValues

    case object Queued     extends ParticipantStatus("queued", isActive = true)
    case object Connecting extends ParticipantStatus("connecting", isActive = true)
    case object Ringing    extends ParticipantStatus("ringing", isActive = true)
    case object Connected  extends ParticipantStatus("connected", isActive = true)
    case object Complete   extends ParticipantStatus("complete", isActive = false)
    case object Failed     extends ParticipantStatus("failed", isActive = false)

    private[client] def fromTwilioStringStatus(s: String): ParticipantStatus = s match {
      case Queued.twilioApiStringRep    => Queued
      case Connected.twilioApiStringRep => Connected
      case Ringing.twilioApiStringRep   => Ringing
      case Connected.twilioApiStringRep => Connected
      case Complete.twilioApiStringRep  => Complete
      case Failed.twilioApiStringRep    => Failed
    }
  }

  final case class Participant(callSid: TwilioCallSid, status: ParticipantStatus)

}
