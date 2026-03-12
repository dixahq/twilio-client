package com.dixa.twilio.model.iam

import com.dixa.twilio.model.EnumWithTwilioString
import com.dixa.twilio.model.general.{Application, ServiceSid}
import com.dixa.twilio.model.video.Room

import scala.collection.immutable

/** Represents a grant that defines which Twilio product and actions the Access Token holder is
  * permitted to use. Each grant is product-specific and is embedded in the Access Token's payload.
  * A single Access Token can carry multiple grants. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
sealed trait TwilioGrant extends EnumWithTwilioString.EnumEntry

object TwilioGrant extends EnumWithTwilioString[TwilioGrant] {

  override def values: immutable.IndexedSeq[TwilioGrant] = findValues

  case class VoiceGrant(
      incomingAllow: Boolean = true,
      outgoingAppSid: Option[Application.Sid] = None
  ) extends TwilioGrant {
    override def twilioString: String = "voice"
  }

  case class ChatGrant(serviceSid: ServiceSid) extends TwilioGrant {
    override def twilioString: String = "chat"
  }

  case class SyncGrant(serviceSid: ServiceSid) extends TwilioGrant {
    override def twilioString: String = "sync"
  }

  case class VideoGrant(room: Option[Room] = None) extends TwilioGrant {
    override def twilioString: String = "video"
  }

  case class RawGrant(grantKey: String, json: String) extends TwilioGrant {
    override def twilioString: String = grantKey
  }
}
