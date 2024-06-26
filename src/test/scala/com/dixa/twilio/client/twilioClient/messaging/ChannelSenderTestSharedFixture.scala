package com.dixa.twilio.client.twilioClient.messaging

import com.dixa.twilio.model.messaging.ChannelSender.Webhooks
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}

trait ChannelSenderTestSharedFixture {
  def channelSenderSid: ChannelSender.Sid = ChannelSenderTestSharedFixture.channelSenderSid
  def whatsappChannelSender: ChannelSender.WhatsappSender =
    ChannelSenderTestSharedFixture.channelSender
}

object ChannelSenderTestSharedFixture {
  val channelSenderSid: ChannelSender.Sid =
    ChannelSender.Sid.unsafe("XEcfd04c72e3397a53e24bd6c7408aff83")
  val channelSender: ChannelSender.WhatsappSender = ChannelSender.WhatsappSender(
    status = ChannelSender.Status.Online,
    profile = ChannelSender.Profile
      .WhatsappProfile(phoneNumberDisplayName = "Dixa Twilio WABA"),
    senderId = WhatsappNumber.unsafe("whatsapp:+4552511283"),
    sid = channelSenderSid,
    webhooks = Webhooks(
      callback = None,
      fallback = None,
      statusCallback = None
    ),
    configuration = ChannelSender.Configuration(wabaId = Some("316806161514452")),
    properties = Some(
      ChannelSender.Properties.WhatsappProperties(
        messagingLimit = None,
        qualityRating = ChannelSender.QualityRating.Unknown
      )
    )
  )
}
