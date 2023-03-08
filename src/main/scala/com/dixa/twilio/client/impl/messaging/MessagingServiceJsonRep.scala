package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.TwilioMessagingService.UseInboundWebhookOnNumber
import com.dixa.twilio.model.messaging.{ServiceSid, StatusCallback, TwilioMessagingService}

import java.net.URL

private[messaging] final case class MessagingServiceJsonRep(
    sid: String,
    account_sid: String,
    friendly_name: Option[String],
    inbound_request_url: Option[String],
    inbound_method: Option[String],
    fallback_url: Option[String],
    fallback_method: Option[String],
    status_callback: Option[String],
    use_inbound_webhook_on_number: Option[Boolean]
) {

  private def toInboundRequestWebhook = {
    val urlAsString = inbound_request_url.getOrElse("")
    if (urlAsString.isEmpty) None
    else
      Some(
        TwilioMessagingService.InboundRequestWebhook(
          HttpMethod.withNameInsensitive(inbound_method.getOrElse("POST")),
          new URL(urlAsString)
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
          new URL(urlAsString)
        )
      )
  }

  private def toStatusCallback = {
    val statusCallbackAsString = status_callback.getOrElse("")
    if (statusCallbackAsString.isEmpty) None
    else Some(StatusCallback(new URL(statusCallbackAsString)))
  }

  private[messaging] def toTwilioMessagingService = TwilioMessagingService(
    ServiceSid(sid),
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
