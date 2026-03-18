// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.messaging.ChannelSenderException
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.messaging.ChannelSender.{VerificationMethod, Webhook}
import com.dixa.twilio.model.messaging.{ChannelSender, MessageRecipient, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

private[messaging] final case class ChannelSenderJsonRep(
    sender_id: String,
    status: String,
    profile: ChannelSenderJsonRep.ProfileJsonRep,
    url: String,
    webhook: ChannelSenderJsonRep.WebhooksJsonRep,
    sid: String,
    configuration: ChannelSenderJsonRep.ConfigurationJsonRep,
    properties: Option[ChannelSenderJsonRep.PropertiesJsonRep]
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
  implicit val ChannelSenderJsonRepReader: Reader[ChannelSenderJsonRep] =
    macroR[ChannelSenderJsonRep]

  def toModel(
      jsonRep: ChannelSenderJsonRep
  ): Either[ChannelSenderException, ChannelSender] = {
    MessageRecipient.fromString(jsonRep.sender_id) match {
      case Some(whatsapp: WhatsappNumber) if jsonRep.configuration.waba_id.isDefined =>
        val status = ChannelSender.Status
          .fromTwilioString(jsonRep.status)
          .getOrElse(ChannelSender.Status.Unknown)
        Right(
          ChannelSender.WhatsappSender(
            status = status,
            profile = ChannelSender.Profile
              .WhatsappProfile(
                about = jsonRep.profile.about,
                phoneNumberDisplayName = jsonRep.profile.name
              ),
            senderId = whatsapp,
            sid = ChannelSender.Sid.unsafe(jsonRep.sid),
            webhooks = toModel(jsonRep.webhook),
            configuration = ChannelSender.Configuration(
              wabaId = jsonRep.configuration.waba_id,
              verificationMethod = jsonRep.configuration.verificationMethod
                .flatMap(VerificationMethod.fromTwilioString)
            ),
            properties = jsonRep.properties.map(toModel)
          )
        )
      case Some(phoneNumber: PhoneNumberE164) =>
        Left(
          ChannelSenderException.ParseFailure(
            s"PhoneNumber Channel Sender with id $phoneNumber not supported"
          )
        )
      case Some(unknown) =>
        Left(
          ChannelSenderException.ParseFailure(s"Unknown Channel Sender $unknown not supported")
        )
      case None =>
        Left(
          ChannelSenderException.ParseFailure(
            s"Channel Sender id ${jsonRep.sender_id} of unknown type not supported"
          )
        )
    }
  }

  private def toModel(
      webHooksJsonRep: WebhooksJsonRep
  ): ChannelSender.Webhooks = {
    ChannelSender.Webhooks(
      fallback = toModel(webHooksJsonRep.fallback_method, webHooksJsonRep.fallback_url),
      statusCallback =
        toModel(webHooksJsonRep.status_callback_method, webHooksJsonRep.status_callback_url),
      callback = toModel(webHooksJsonRep.callback_method, webHooksJsonRep.callback_url)
    )
  }

  private def toModel(
      methodString: Option[String],
      urlString: Option[String]
  ): Option[ChannelSender.Webhook] = {
    (methodString.flatMap(HttpMethod.fromTwilioString), urlString) match {
      case (Some(method: HttpMethod), Some(url)) => Option(Webhook(method, url))
      case _                                     => None
    }
  }

  private def toModel(
      qualityRatingJsonRep: PropertiesJsonRep
  ): ChannelSender.Properties.WhatsappProperties = {
    val messagingLimit = qualityRatingJsonRep.messaging_limit.flatMap {
      case limit if limit.isEmpty => None
      case limit                  => Some(limit)
    }
    qualityRatingJsonRep.quality_rating.flatMap(
      ChannelSender.QualityRating.fromTwilioString
    ) match {
      case Some(qualityRating) =>
        ChannelSender.Properties.WhatsappProperties(
          messagingLimit = messagingLimit,
          qualityRating = qualityRating
        )
      case _ =>
        ChannelSender.Properties.WhatsappProperties(
          messagingLimit = messagingLimit,
          qualityRating = ChannelSender.QualityRating.Unknown
        )
    }
  }
}
