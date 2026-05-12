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

import com.dixa.twilio.client.ApiException.{BadRequestException, Conflict, NotFound}
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString,
  TwilioClientPickler,
  TwilioInternalErrorJsonRep
}
import com.dixa.twilio.client.messaging.{
  ChannelSendersException,
  ChannelsSendersCreateRequestExecutor
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.messaging.ChannelSender
import com.dixa.twilio.client.impl.messaging.WhatsappSenderCreateJsonRep._
import com.dixa.twilio.client.messaging.ChannelSendersException.Api
import com.dixa.twilio.client.messaging.ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest
import com.dixa.twilio.model.messaging.MessageSender.{E164, Whatsapp}
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

private[messaging] final class ChannelsSendersCreateRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelsSendersCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelsSendersCreateRequestExecutor.ChannelSendersCreateRequest
  ): Either[ChannelSendersException, HttpRequest] = {
    val jsonBodyEither = (req.senderId, req.profile) match {
      case (
            number: Whatsapp,
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
      case (_: E164, _) =>
        Left(ChannelSendersException.ChannelSenderNotSupported("PhoneNumberE164"))
      case _ => Left(ChannelSendersException.ChannelSenderNotSupported("Unknown"))
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
  ): ApiExceptionWrapper =
    ChannelSendersException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ChannelSendersException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ChannelSendersCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSendersException, ChannelSender] = {
    httpResponse.status match {
      case StatusCodes.NotFound   => Left(Api(NotFound(entity.toString)))
      case StatusCodes.BadRequest => Left(Api(BadRequestException(entity.toString)))
      case StatusCodes.Conflict   =>
        entity.parse[DefaultApiErrorEntityJsonRep]() match {
          case Left(_)                => Left(Api(Conflict(Some(entity.toString))))
          case Right(defaultApiError) =>
            (defaultApiError.code, defaultApiError.message) match {
              case (63100L, "sender_id provided already exists") =>
                Left(
                  ChannelSendersException.SenderIdAlreadyExists(
                    request.senderId.twilioString,
                    defaultApiError.message,
                    defaultApiError.more_info
                  )
                )
              case (63103L, "Could not extend credit line to the waba_id provided") =>
                Left(
                  ChannelSendersException.CouldNotExtendCreditLine(
                    request.configuration.wabaId,
                    defaultApiError.message,
                    defaultApiError.more_info
                  )
                )
              case _ => Left(Api(Conflict(Some(entity.toString))))
            }
        }
      case StatusCodes.InternalServerError =>
        entity.parse[TwilioInternalErrorJsonRep]() match {
          case Left(_) =>
            Left(
              ChannelSendersException.TwilioInternalError(
                errorCode = None,
                errorMessage = None,
                moreInfo = None,
                rawResponse = entity.toString
              )
            )
          case Right(errorRep) =>
            Left(
              ChannelSendersException.TwilioInternalError(
                errorCode = errorRep.code,
                errorMessage = errorRep.message,
                moreInfo = errorRep.more_info,
                rawResponse = entity.toString
              )
            )
        }
      case StatusCodes.OK | StatusCodes.NoContent | StatusCodes.Accepted => parseBody(entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def parseBody(entity: HttpEntityString) = {
    entity.parse[ChannelSenderJsonRep]() match {
      case Left(ex) =>
        Left(
          ChannelSendersException.ParseFailure(ex.cause.getMessage)
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
