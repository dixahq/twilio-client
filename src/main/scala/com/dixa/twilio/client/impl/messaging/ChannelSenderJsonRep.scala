package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

private[messaging] final case class ChannelSenderJsonRep(
    sender_id: String,
    status: String,
    profile: ChannelSenderJsonRep.ProfileJsonRep,
    url: String,
    webhook: ChannelSenderJsonRep.WebhooksJsonRep,
    sid: String,
    configuration: ChannelSenderJsonRep.ConfigurationJsonRep,
    properties: ChannelSenderJsonRep.PropertiesJsonRep
)

private[messaging] object ChannelSenderJsonRep {

  final case class WebhooksJsonRep(
      fallback_method: Option[String] = None,
      fallback_url: Option[String] = None,
      status_callback_url: Option[String] = None,
      status_callback_method: Option[String] = None,
      callback_method: Option[String] = None,
      callback_url: Option[String] = None
  )
  final case class ProfileJsonRep(about: Option[String] = None, name: String)

  final case class ConfigurationJsonRep(
      waba_id: Option[String] = None,
      verificationMethod: Option[String] = None
  )

  final case class PropertiesJsonRep(
      quality_rating: Option[String] = None,
      messaging_limit: Option[String] = None,
  )

  implicit val webhooksJsonRepReader: Reader[WebhooksJsonRep] =
    macroR[WebhooksJsonRep]
  implicit val profileJsonRepReader: Reader[ProfileJsonRep] =
    macroR[ProfileJsonRep]
  implicit val configurationJsonRepReader: Reader[ConfigurationJsonRep] =
    macroR[ConfigurationJsonRep]
  implicit val propertiesJsonRepReader: Reader[PropertiesJsonRep] =
    macroR[PropertiesJsonRep]
  implicit val channelSenderJsonRepReader: Reader[ChannelSenderJsonRep] =
    macroR[ChannelSenderJsonRep]
}
