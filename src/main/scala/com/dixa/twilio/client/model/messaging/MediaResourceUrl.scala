package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.TwilioMessage.MessageSid

final case class MediaResourceUrl(override val toString: String)

object MediaResourceUrl {

  private[client] def buildMediaResourcePath(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid
  ): String = {
    buildMediaResourceBasePath(accountSid, messageSid) + "/Media.json"
  }

  private[client] def buildMediaResourceBasePath(
      accountSid: TwilioAccount.Sid,
      messageSid: MessageSid
  ): String = {
    s"/2010-04-01/Accounts/$accountSid/Messages/$messageSid"
  }

}
