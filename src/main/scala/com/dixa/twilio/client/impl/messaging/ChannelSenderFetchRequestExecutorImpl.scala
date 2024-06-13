package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.messaging.ChannelSenderJsonRep.WebhooksJsonRep
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelSenderFetchRequestExecutor
import com.dixa.twilio.client.messaging.ChannelSenderFetchRequestExecutor.ChannelSenderFetchException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.messaging.ChannelSender.Webhook
import com.dixa.twilio.model.messaging.{ChannelSender, MessageRecipient, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] class ChannelSenderFetchRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest
  ): Either[ChannelSenderFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.channelSenderSid}",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelSenderFetchException.Api =
    ChannelSenderFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderFetchException.Unspecified = ChannelSenderFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ChannelSenderFetchRequestExecutor.ChannelSenderFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderFetchException, ChannelSender] = {
    entity.parse[ChannelSenderJsonRep]() match {
      case Left(ex) =>
        Left(
          ChannelSenderFetchException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))
        )
      case Right(decoded: ChannelSenderJsonRep) => toModel(decoded)
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
      jsonRep: ChannelSenderJsonRep
  ): Either[ChannelSenderFetchException, ChannelSender] = {
    MessageRecipient.fromString(jsonRep.sender_id) match {
      case Some(whatsapp: WhatsappNumber) if jsonRep.configuration.waba_id.isDefined =>
        val status = ChannelSender.Status
          .fromTwilioString(jsonRep.status)
          .getOrElse(ChannelSender.Status.Unknown)
        Right(
          ChannelSender.WhatsappSender(
            status = status,
            profile = ChannelSender.Profile(jsonRep.profile.name),
            senderId = whatsapp,
            sid = ChannelSender.Sid.unsafe(jsonRep.sid),
            webhooks = toModel(jsonRep.webhook),
            configuration = ChannelSender.Configuration.WabaId(jsonRep.configuration.waba_id.get)
          )
        )
      case Some(phoneNumber: PhoneNumberE164) =>
        Left(
          ChannelSenderFetchException.ParseFailure(
            s"PhoneNumber Channel Sender with id $phoneNumber not supported"
          )
        )
      case Some(unknown) =>
        Left(
          ChannelSenderFetchException.ParseFailure(s"Unknown Channel Sender $unknown not supported")
        )
      case None =>
        Left(
          ChannelSenderFetchException.ParseFailure(
            s"Channel Sender id ${jsonRep.sender_id} of unknown type not supported"
          )
        )
    }
  }
}
