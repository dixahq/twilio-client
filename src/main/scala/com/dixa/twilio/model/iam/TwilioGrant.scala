package com.dixa.twilio.model.iam

import com.dixa.twilio.model.general.{Application, ServiceSid}
import com.dixa.twilio.model.video.Room

/** Represents a grant that defines which Twilio product and actions the Access Token holder is
  * permitted to use. Each grant is product-specific and is embedded in the Access Token's payload.
  * A single Access Token can carry multiple grants. More info:
  * https://www.twilio.com/docs/iam/access-tokens
  */
sealed trait TwilioGrant

object TwilioGrant {
  case class VoiceGrant(
      incomingAllow: Boolean = true,
      outgoingAppSid: Option[Application.Sid] = None
  ) extends TwilioGrant

  case class ChatGrant(serviceSid: ServiceSid)        extends TwilioGrant
  case class SyncGrant(serviceSid: ServiceSid)        extends TwilioGrant
  case class VideoGrant(room: Option[Room] = None)    extends TwilioGrant
  case class RawGrant(grantKey: String, json: String) extends TwilioGrant
}
