package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.TwilioMessage.MessageSid

import java.time.Instant

final case class MediaResourceReference(
    sid: MediaSid,
    accountSid: TwilioAccount.Sid,
    parentSid: MessageSid,
    contentType: String,
    dateCreated: Instant,
    dateUpdated: Instant
) {
  private val basePath: String = MediaResourceUrl.buildMediaResourceBasePath(accountSid, parentSid)

  // Constructs the final url that contains the media resource,
  // This gives the power to clients to fetch the resources, without need of
  // using Twilio's basic auth, since it's publicly available.
  def resourceUrl(twilioConnSettings: TwilioConnectionSettings): MediaResourceUrl =
    MediaResourceUrl(
      s"https://${twilioConnSettings.hostNameFor(ApiSubDomain.Api)}$basePath/Media/$sid"
    )
}
