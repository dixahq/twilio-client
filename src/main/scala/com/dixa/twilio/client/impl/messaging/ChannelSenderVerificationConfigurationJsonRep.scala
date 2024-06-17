package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging.ChannelSenderVerificationConfigurationJsonRep.ConfigurationJsonRep
import upickle.default.{macroW, Writer}

private[messaging] final case class ChannelSenderVerificationConfigurationJsonRep(
    configuration: ConfigurationJsonRep,
)

private[messaging] object ChannelSenderVerificationConfigurationJsonRep {

  final case class ConfigurationJsonRep(verification_code: String)

  final case class PropertiesJsonRep(
      quality_rating: Option[String],
      messaging_limit: Option[String],
  )

  implicit val configurationJsonRepReader: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val channelSenderJsonRepReader: Writer[ChannelSenderVerificationConfigurationJsonRep] =
    macroW[ChannelSenderVerificationConfigurationJsonRep]
}
