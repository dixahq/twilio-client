package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{MediaResourceUrl, MediaSid, MessageSid}

private[client] object MediaResourceUrlFactory {

  private[client] def buildMediaResourcePath(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid
  ): String = {
    buildMediaResourceBasePath(accountSid, messageSid) + "/Media.json"
  }

  private def buildMediaResourceBasePath(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid
  ): String = {
    s"/2010-04-01/Accounts/$accountSid/Messages/$messageSid"
  }

  // Constructs the final url that contains the media resource,
  // This gives the power to clients to fetch the resources, without need of
  // using Twilio's basic auth, since it's publicly available.
  private[messaging] def resourceUrl(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid,
      sid: MediaSid,
      twilioConnSettings: TwilioConnectionSettings
  ): MediaResourceUrl = {
    val basePath: String = buildMediaResourceBasePath(accountSid, messageSid)
    MediaResourceUrl(
      s"${twilioConnSettings.protocol}://${twilioConnSettings.hostNameFor(ApiSubDomain.Api)}$basePath/Media/$sid"
    )
  }
}
