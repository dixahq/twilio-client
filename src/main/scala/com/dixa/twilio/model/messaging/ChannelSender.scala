package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod, SidAbstract}
import com.dixa.twilio.model.SidAbstract.Prefix

import scala.collection.immutable

/** Channel Sender represents a sender that can send messages through a specific channel
  */
sealed trait ChannelSender {
  val status: ChannelSender.Status
  val profile: ChannelSender.Profile
  val senderId: MessageRecipient
  val sid: ChannelSender.Sid
  val webhooks: ChannelSender.Webhooks
  val configuration: ChannelSender.Configuration
  val properties: ChannelSender.Properties
}

object ChannelSender {

  final case class WhatsappSender(
      status: ChannelSender.Status,
      profile: ChannelSender.Profile.WhatsappProfile,
      senderId: WhatsappNumber,
      sid: ChannelSender.Sid,
      webhooks: ChannelSender.Webhooks,
      configuration: ChannelSender.Configuration,
      properties: ChannelSender.Properties.WhatsappProperties
  ) extends ChannelSender

  final case class Sid private[ChannelSender] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("XE")), new Sid(_))

  sealed abstract class Status(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Online                    extends Status("ONLINE")
    case object OnlineUpdating            extends Status("ONLINE:UPDATING")
    case object OnlinePendingVerification extends Status("ONLINE:PENDING_VERIFICATION")
    case object Offline                   extends Status("OFFLINE")
    case object Creating                  extends Status("CREATING")
    case object Verifying                 extends Status("VERIFYING")
    case object PendingVerification       extends Status("PENDING_VERIFICATION")
    case object Unknown                   extends Status("UNKNOWN")
  }

  sealed trait Profile

  object Profile {

    /** The profile of the sender in the case of Waba only containing the waba name.
      */
    final case class WhatsappProfile(about: Option[String] = None, phoneNumberDisplayName: String)
        extends Profile
  }

  sealed abstract class VerificationMethod(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object VerificationMethod extends EnumWithTwilioString[VerificationMethod] {
    override val values: immutable.IndexedSeq[VerificationMethod] = findValues

    case object SMS   extends VerificationMethod("sms")
    case object Voice extends VerificationMethod("voice")
  }

  final case class Configuration(
      wabaId: Option[String] = None,
      verificationMethod: Option[VerificationMethod] = None
  )

  sealed abstract class QualityRating(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object QualityRating extends EnumWithTwilioString[QualityRating] {
    override val values: immutable.IndexedSeq[QualityRating] = findValues

    case object High    extends QualityRating("HIGH")
    case object Unknown extends QualityRating("UNKNOWN")
  }

  sealed trait Properties

  object Properties {
    final case class WhatsappProperties(
        messagingLimit: Option[String],
        qualityRating: QualityRating
    ) extends Properties
  }

  case class Webhook(method: HttpMethod, url: String)

  case class Webhooks(
      fallback: Option[Webhook],
      statusCallback: Option[Webhook],
      callback: Option[Webhook]
  )
}
