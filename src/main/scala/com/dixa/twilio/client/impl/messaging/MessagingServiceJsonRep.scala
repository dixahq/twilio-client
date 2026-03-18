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
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.TwilioMessagingService
import com.dixa.twilio.model.messaging.TwilioMessagingService.UseInboundWebhookOnNumber

import java.net.URI

private[messaging] final case class MessagingServiceJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: Option[String] = None,
    inbound_request_url: Option[String] = None,
    inbound_method: Option[String] = None,
    fallback_url: Option[String] = None,
    fallback_method: Option[String] = None,
    status_callback: Option[String] = None,
    use_inbound_webhook_on_number: Option[Boolean] = None
) {

  private def toInboundRequestWebhook = {
    val urlAsString = inbound_request_url.getOrElse("")
    if (urlAsString.isEmpty) None
    else
      Some(
        TwilioMessagingService.InboundRequestWebhook(
          HttpMethod.withNameInsensitive(inbound_method.getOrElse("POST")),
          new URI(urlAsString).toURL
        )
      )
  }

  private def toFallbackHook = {
    val urlAsString = fallback_url.getOrElse("")
    if (urlAsString.isEmpty) None
    else
      Some(
        TwilioMessagingService.FallbackWebhook(
          HttpMethod.withNameInsensitive(fallback_method.getOrElse("POST")),
          new URI(urlAsString).toURL
        )
      )
  }

  private def toStatusCallback = {
    val statusCallbackAsString = status_callback.getOrElse("")
    if (statusCallbackAsString.isEmpty) None
    else Some(MessageStatusCallback(new URI(statusCallbackAsString).toURL))
  }

  private[messaging] def toTwilioMessagingService = TwilioMessagingService(
    TwilioMessagingService.Sid.unsafe(sid),
    TwilioAccount.Sid.unsafe(account_sid),
    TwilioMessagingService.FriendlyName(friendly_name.getOrElse("")),
    toInboundRequestWebhook,
    toFallbackHook,
    toStatusCallback,
    UseInboundWebhookOnNumber.fromBoolean(
      use_inbound_webhook_on_number.getOrElse(false)
    )
  )
}

private[messaging] object MessagingServiceJsonRep {

  implicit val messagingServiceJsonRepReader: Reader[MessagingServiceJsonRep] =
    macroR[MessagingServiceJsonRep]
}
