package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[messaging] final case class ChannelSenderJsonRep(
    sender_id: String,
    status: String,
    profile: ChannelSenderJsonRep.ProfileJsonRep,
    url: String,
    webhook: ChannelSenderJsonRep.WebhooksJsonRep,
    sid: String,
    configuration: ChannelSenderJsonRep.ConfigurationJsonRep
)

private[messaging] object ChannelSenderJsonRep {

  final case class WebhooksJsonRep(
      fallback_method: Option[String],
      fallback_url: Option[String],
      status_callback_url: Option[String],
      status_callback_method: Option[String],
      callback_method: Option[String],
      callback_url: Option[String]
  )
  final case class ProfileJsonRep(name: String)

  final case class ConfigurationJsonRep(waba_id: Option[String])

  implicit val webhooksJsonRepReader: Reader[WebhooksJsonRep] =
    macroR[WebhooksJsonRep]
  implicit val ProfileJsonRepReader: Reader[ProfileJsonRep] =
    macroR[ProfileJsonRep]
  implicit val ConfigurationJsonRepReader: Reader[ConfigurationJsonRep] =
    macroR[ConfigurationJsonRep]
  implicit val channelSenderJsonRepReader: Reader[ChannelSenderJsonRep] =
    macroR[ChannelSenderJsonRep]
}
