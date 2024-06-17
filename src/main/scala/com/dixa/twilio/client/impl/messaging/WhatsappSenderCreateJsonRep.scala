package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging
import upickle.default.{macroW, Writer}

private[messaging] final case class WhatsappSenderCreateJsonRep(
    sender_id: String,
    profile: WhatsappSenderCreateJsonRep.ProfileJsonRep,
    webhook: messaging.WhatsappSenderCreateJsonRep.WebhooksJsonRep,
    configuration: WhatsappSenderCreateJsonRep.ConfigurationJsonRep,
)

private[messaging] object WhatsappSenderCreateJsonRep {

  final case class WebhooksJsonRep(
      fallback_method: String,
      fallback_url: String,
      status_callback_url: String,
      status_callback_method: String,
      callback_method: String,
      callback_url: String
  )
  final case class ProfileJsonRep(about: String, name: String)

  final case class ConfigurationJsonRep(verification_method: String)

  implicit val webhooksJsonRepWriter: Writer[WebhooksJsonRep] =
    macroW[WebhooksJsonRep]
  implicit val profileJsonRepWriter: Writer[ProfileJsonRep] =
    macroW[ProfileJsonRep]
  implicit val configurationJsonRepWriter: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val whatsappSenderCreateJsonRepWriter: Writer[WhatsappSenderCreateJsonRep] =
    macroW[WhatsappSenderCreateJsonRep]
}
