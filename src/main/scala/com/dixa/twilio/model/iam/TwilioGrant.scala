package com.dixa.twilio.model.iam

import com.dixa.twilio.model.general.Application

/** Represents a grant that defines which Twilio product and actions the Access Token holder is
  * permitted to use. Each grant is product-specific and is embedded in the Access Token's payload.
  * A single Access Token can carry multiple grants. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
sealed trait TwilioGrant {
  def grantKey: String
  def toJson: String
}

object TwilioGrant {

  final case class VoiceGrant(
      incomingAllow: Boolean,
      outgoingAppSid: Option[Application.Sid] // TwiML app
  ) extends TwilioGrant {
    val grantKey       = "voice"
    def toJson: String = {
      val incoming = s""""incoming":{"allow":$incomingAllow}"""
      val outgoing = outgoingAppSid
        .map(sid => s""","outgoing":{"application_sid":"${sid.toString}"}""")
        .getOrElse("")
      s"{$incoming$outgoing}"
    }
  }

  final case class ChatGrant(
      serviceSid: String
  ) extends TwilioGrant {
    val grantKey       = "chat"
    def toJson: String =
      s"""{"service_sid":"$serviceSid"}"""
  }

  final case class SyncGrant(
      serviceSid: String
  ) extends TwilioGrant {
    val grantKey       = "sync"
    def toJson: String =
      s"""{"service_sid":"$serviceSid"}"""
  }

  final case class VideoGrant(
      room: Option[String]
  ) extends TwilioGrant {
    val grantKey       = "video"
    def toJson: String =
      room.map(r => s"""{"room":"$r"}""").getOrElse("{}")
  }

  final case class RawGrant(
      grantKey: String,
      json: String
  ) extends TwilioGrant {
    def toJson: String = json
  }
}
