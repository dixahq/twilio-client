// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.messaging.ChannelsSendersCommonExceptions
import com.dixa.twilio.client.messaging.ChannelsSendersCreateRequestExecutor.ChannelsSendersException
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.messaging.ChannelSender.{VerificationMethod, Webhook}
import com.dixa.twilio.model.messaging.MessageSender.{E164, Whatsapp}
import com.dixa.twilio.model.messaging.{ChannelSender, MessageSender}

private[messaging] final case class ChannelsSendersJsonRep(
    sender_id: String,
    status: String,
    profile: ChannelsSendersJsonRep.ProfileJsonRep,
    url: String,
    webhook: ChannelsSendersJsonRep.WebhooksJsonRep,
    sid: String,
    configuration: ChannelsSendersJsonRep.ConfigurationJsonRep,
    properties: Option[ChannelsSendersJsonRep.PropertiesJsonRep],
    // Option, not a plain Seq with a default: Twilio sends `"offline_reasons": null` for a sender
    // that is not offline, and a default only applies when the key is absent. Read into a bare Seq
    // the null is assigned as-is, so the field holds Java null and blows up on first use rather than
    // failing to parse. Only OptionReader here handles visitNull (see TwilioClientPickler).
    offline_reasons: Option[Seq[ChannelsSendersJsonRep.OfflineReasonJsonRep]] = None
)

private[messaging] object ChannelsSendersJsonRep {

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

  final case class PropertiesJsonRep(
      quality_rating: Option[String] = None,
      messaging_limit: Option[String] = None,
  )

  /** Twilio sends `code` as a string (e.g. `"410"`), not a number. */
  final case class OfflineReasonJsonRep(
      code: Option[String] = None,
      message: Option[String] = None,
      more_info: Option[String] = None
  )

  implicit val webhooksJsonRepReader: Reader[WebhooksJsonRep] =
    macroR[WebhooksJsonRep]
  implicit val profileJsonRepReader: Reader[ProfileJsonRep] =
    macroR[ProfileJsonRep]
  implicit val configurationJsonRepReader: Reader[ConfigurationJsonRep] =
    macroR[ConfigurationJsonRep]
  implicit val propertiesJsonRepReader: Reader[PropertiesJsonRep] =
    macroR[PropertiesJsonRep]
  implicit val offlineReasonJsonRepReader: Reader[OfflineReasonJsonRep] =
    macroR[OfflineReasonJsonRep]
  implicit val channelsSendersJsonRepReader: Reader[ChannelsSendersJsonRep] =
    macroR[ChannelsSendersJsonRep]

  private def toModel(
      jsonRep: ChannelsSendersJsonRep,
      sender: MessageSender
  ): ChannelSender = {
    val status = ChannelSender.Status
      .fromTwilioString(jsonRep.status)
      .getOrElse(ChannelSender.Status.Unknown)
    ChannelSender.WhatsappSender(
      status = status,
      profile = ChannelSender.Profile
        .WhatsappProfile(
          about = jsonRep.profile.about,
          phoneNumberDisplayName = jsonRep.profile.name
        ),
      senderId = sender,
      sid = ChannelSender.Sid.unsafe(jsonRep.sid),
      webhooks = toModel(jsonRep.webhook),
      configuration = ChannelSender.Configuration(
        wabaId = jsonRep.configuration.waba_id,
        verificationMethod = jsonRep.configuration.verification_method
          .flatMap(VerificationMethod.fromTwilioString)
      ),
      properties = jsonRep.properties.map(toModel),
      offlineReasons = jsonRep.offline_reasons.getOrElse(Seq.empty).map(toModel)
    )
  }

  private def toModel(
      offlineReasonJsonRep: OfflineReasonJsonRep
  ): ChannelSender.OfflineReason =
    ChannelSender.OfflineReason(
      code = offlineReasonJsonRep.code,
      message = offlineReasonJsonRep.message,
      moreInfo = offlineReasonJsonRep.more_info
    )

  def toModelOldExceptionHandling(
      jsonRep: ChannelsSendersJsonRep
  ): Either[ChannelsSendersCommonExceptions, ChannelSender] = {
    MessageSender.fromString(jsonRep.sender_id) match {
      case Right(whatsapp: Whatsapp) if jsonRep.configuration.waba_id.isDefined =>
        Right(toModel(jsonRep, whatsapp))
      case Right(senderE164: E164) =>
        Left(
          ChannelsSendersCommonExceptions.ParseFailure(
            s"PhoneNumber Channel Sender with ID: ${senderE164.asString} not supported"
          )
        )
      case Right(other) =>
        Left(
          ChannelsSendersCommonExceptions.ParseFailure(
            s"Channel Sender with ID: ${other.asString} is not supported"
          )
        )
      case Left(_) =>
        Left(
          ChannelsSendersCommonExceptions.ParseFailure(
            s"Channel Sender ID: ${jsonRep.sender_id} of unknown type is not supported"
          )
        )
    }
  }

  def toModelNewExceptionHandling(
      jsonRep: ChannelsSendersJsonRep
  ): Either[ChannelsSendersException, ChannelSender] = {
    MessageSender.fromString(jsonRep.sender_id) match {
      case Right(whatsapp: Whatsapp) if jsonRep.configuration.waba_id.isDefined =>
        Right(toModel(jsonRep, whatsapp))
      case Right(unsupportedSender) =>
        Left(
          ChannelsSendersException.ChannelSenderNotSupported(unsupportedSender.asString)
        )
      case Left(_) =>
        Left(
          ChannelsSendersException.ChannelSenderNotSupported(
            s"Unknown Channel Sender ID: ${jsonRep.sender_id}"
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
