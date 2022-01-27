package com.dixa.twilio.client.implDetails.request.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.implDetails.TwilioUri.TwilioPath
import com.dixa.twilio.client.implDetails.{ApiSubDomain, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.model.HttpMethod
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import io.circe.generic.auto._

import java.net.URL

private[implDetails] final class ReadServicesRequest()(
    implicit httpExt: HttpExt,
    materializer: Materializer
) {

  def apply(conSettings: TwilioConnectionSettings): Source[TwilioMessagingService, NotUsed] = {
    TwilioPagingFlow
      .createPagingSrc(
        conSettings,
        TwilioPath(ApiSubDomain.Messaging, HttpMethods.GET, "/v1/Services?PageSize=1000")
      )
      .map(entityToServiceList)
      .mapConcat(identity)
  }

  // It actually looks like Twilio tries to avoid null values in this API, and use empty
  // strings or default values instead, but lets
  // make all the optional attributes options anyway, just to be on the safe side.
  private final case class ServiceJsonRep(
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
      else Some(TwilioMessagingService.StatusCallback(new URL(statusCallbackAsString)))
    }

    private[ReadServicesRequest] def toTwilioMessagingService = TwilioMessagingService(
      TwilioMessagingService.Sid(sid),
      TwilioAccount.Sid(account_sid),
      TwilioMessagingService.FriendlyName(friendly_name.getOrElse("")),
      toInboundRequestWebhook,
      toFallbackHook,
      toStatusCallback,
      TwilioMessagingService.UseInboundWebhookOnNumber.fromBoolean(
        use_inbound_webhook_on_number.getOrElse(false)
      )
    )
  }
  private final case class OuterJsonRep(services: List[ServiceJsonRep])

  private def entityToServiceList(entity: HttpEntityString): Seq[TwilioMessagingService] = {
    val decoded = entity.parseUnsafe[OuterJsonRep]()
    decoded.services.map(_.toTwilioMessagingService)
  }
}
