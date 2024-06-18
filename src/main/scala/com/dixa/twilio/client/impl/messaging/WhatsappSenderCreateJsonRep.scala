package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroW, Writer}

private[messaging] final case class WhatsappSenderCreateJsonRep(
    sender_id: String,
    profile: WhatsappSenderCreateJsonRep.ProfileJsonRep,
    webhook: WhatsappSenderCreateJsonRep.WebhooksJsonRep,
    configuration: WhatsappSenderCreateJsonRep.ConfigurationJsonRep,
)

private[messaging] object WhatsappSenderCreateJsonRep {

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
      verification_method: Option[String] = None
  )

  implicit val webhooksJsonRepWriter: Writer[WebhooksJsonRep] = macroW[WebhooksJsonRep]
  implicit val profileJsonRepWriter: Writer[ProfileJsonRep]   = macroW[ProfileJsonRep]
  implicit val configurationJsonRepWriter: Writer[ConfigurationJsonRep] =
    macroW[ConfigurationJsonRep]
  implicit val whatsappSenderCreateJsonRepWriter: Writer[WhatsappSenderCreateJsonRep] =
    macroW[WhatsappSenderCreateJsonRep]
}
