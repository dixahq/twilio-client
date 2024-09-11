package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.ApiException.BadRequestException
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, TwilioClientPickler}
import com.dixa.twilio.client.messaging.{ChannelSenderCreateRequestExecutor, ChannelSenderException}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.messaging.{ChannelSender, WhatsappNumber}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.impl.messaging.WhatsappSenderCreateJsonRep._
import com.dixa.twilio.client.messaging.ChannelSenderException.Api
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] class ChannelSenderCreateRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelSenderCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.POST

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest
  ): Either[ChannelSenderException, HttpRequest] = {
    val jsonBodyEither = (req.senderId, req.profile) match {
      case (
            number: WhatsappNumber,
            profile: ChannelSender.Profile.WhatsappProfile,
          ) =>
        Right(
          WhatsappSenderCreateJsonRep(
            sender_id = number.twilioString,
            profile = toJson(profile),
            webhook = toJson(req.webhooks),
            configuration = toJson(req.configuration)
          )
        )
      case (_: PhoneNumberE164, _) =>
        Left(ChannelSenderException.ChannelNotSupported("PhoneNumberE164"))
      case _ => Left(ChannelSenderException.ChannelNotSupported("Unknown"))
    }
    jsonBodyEither.flatMap(jsonBody =>
      createHttpRequestFor(
        s"/${ApiVersion.V2}/Channels/Senders",
        connSettings
      ).map(
        _.withEntity(
          HttpEntity(
            ContentTypes.`application/json`,
            TwilioClientPickler.write[WhatsappSenderCreateJsonRep](jsonBody)
          )
        )
      )
    )

  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelSenderException.Api =
    ChannelSenderException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSenderException.Unspecified = ChannelSenderException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ChannelSenderCreateRequestExecutor.ChannelSenderCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSenderException, ChannelSender] = {
    httpResponse.status match {
      case StatusCodes.BadRequest => Left(Api(BadRequestException(entity.toString)))
      case _                      => parseBody(entity)
    }
  }

  private def parseBody(entity: HttpEntityString) = {
    entity.parse[ChannelSenderJsonRep]() match {
      case Left(ex) =>
        Left(
          ChannelSenderException.ParseFailure(ex.cause.getMessage)
        )
      case Right(decoded: ChannelSenderJsonRep) => ChannelSenderJsonRep.toModel(decoded)
    }
  }

  private def toJson(
      profile: ChannelSender.Profile.WhatsappProfile
  ): WhatsappSenderCreateJsonRep.ProfileJsonRep = {
    WhatsappSenderCreateJsonRep.ProfileJsonRep(
      about = profile.about,
      name = profile.phoneNumberDisplayName
    )
  }

  private def toJson(
      webhooks: ChannelSender.Webhooks
  ): WhatsappSenderCreateJsonRep.WebhooksJsonRep = {
    val (fallbackMethod, fallbackUrl): (Option[String], Option[String]) = toJson(webhooks.fallback)
    val (statusCallbackMethod, statusCallbackUrl): (Option[String], Option[String]) = toJson(
      webhooks.statusCallback
    )
    val (callbackMethod, callbackUrl): (Option[String], Option[String]) = toJson(webhooks.callback)
    WhatsappSenderCreateJsonRep.WebhooksJsonRep(
      fallback_method = fallbackMethod,
      fallback_url = fallbackUrl,
      status_callback_url = statusCallbackUrl,
      status_callback_method = statusCallbackMethod,
      callback_method = callbackMethod,
      callback_url = callbackUrl
    )
  }

  private def toJson(
      webhook: Option[ChannelSender.Webhook]
  ): (Option[String], Option[String]) =
    webhook.map(hook => (Some(hook.method.twilioString), Some(hook.url))).getOrElse((None, None))

  private def toJson(
      configuration: ChannelSender.Configuration
  ) =
    WhatsappSenderCreateJsonRep.ConfigurationJsonRep(
      waba_id = configuration.wabaId,
      verification_method = configuration.verificationMethod.map(_.twilioString)
    )
}
