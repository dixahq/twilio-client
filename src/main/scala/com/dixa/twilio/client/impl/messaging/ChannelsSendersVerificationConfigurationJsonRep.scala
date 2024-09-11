package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging.ChannelsSendersVerificationConfigurationJsonRep.ConfigurationJsonRep
import upickle.default.{macroW, Writer}

private[messaging] final case class ChannelsSendersVerificationConfigurationJsonRep(
    configuration: ConfigurationJsonRep,
)

private[messaging] object ChannelsSendersVerificationConfigurationJsonRep {

  final case class ConfigurationJsonRep(verification_code: String)

  final case class PropertiesJsonRep(
      quality_rating: Option[String],
      messaging_limit: Option[String],
  )

  implicit val configurationJsonRepReader: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val channelSenderJsonRepReader: Writer[ChannelsSendersVerificationConfigurationJsonRep] =
    macroW[ChannelsSendersVerificationConfigurationJsonRep]
}
