package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.{EnumWithTwilioString, SidAbstract}
import com.dixa.twilio.model.SidAbstract.Prefix

import scala.collection.immutable

sealed trait ChannelSender {
  val status: ChannelSender.Status
  val profile: ChannelSender.Profile
  val senderId: MessageRecipient
  val sid: ChannelSender.Sid
  val configuration: ChannelSender.Configuration
}

object ChannelSender {

  final case class WhatsappSender(
      status: ChannelSender.Status,
      profile: ChannelSender.Profile,
      senderId: WhatsappNumber,
      sid: ChannelSender.Sid,
      configuration: ChannelSender.Configuration.WabaId
  ) extends ChannelSender

  final case class Sid private[ChannelSender] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("XE")), new Sid(_))

  sealed abstract class Status(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Online  extends Status("ONLINE")
    case object Unknown extends Status("UNKNOWN")
  }

  final case class Profile(name: String)

  sealed trait Configuration

  object Configuration {
    final case class WabaId(wabaId: String) extends Configuration
  }
}
