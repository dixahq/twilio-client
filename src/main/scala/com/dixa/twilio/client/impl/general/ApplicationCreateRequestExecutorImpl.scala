package com.dixa.twilio.client.impl.general

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.ApplicationCreateRequestExecutor
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.general.Application

import scala.concurrent.ExecutionContext

private[client] class ApplicationCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ApplicationCreateRequestExecutor {

  import ApplicationCreateRequestExecutor._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ApplicationCreateRequest
  ): Either[ApplicationCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid)
      .withOptionalParam("VoiceUrl", req.voiceUrl)
      .withOptionalParam("VoiceMethod", req.voiceMethod)
      .withOptionalParam("VoiceFallbackUrl", req.voiceFallbackUrl)
      .withOptionalParam("VoiceFallbackMethod", req.voiceFallbackMethod)
      .withOptionalParam("StatusCallback", req.statusCallback)
      .withOptionalParam("StatusCallbackMethod", req.statusCallbackMethod)
      .withOptionalBooleanParam("VoiceCallerIdLookup", req.voiceCallerIdLookup)
      .withOptionalParam("SmsUrl", req.smsUrl)
      .withOptionalParam("SmsMethod", req.smsMethod)
      .withOptionalParam("SmsFallbackUrl", req.smsFallbackUrl)
      .withOptionalParam("SmsFallbackMethod", req.smsFallbackMethod)
      .withOptionalParam("SmsStatusCallback", req.smsStatusCallback)
      .withOptionalParam("MessageStatusCallback", req.messageStatusCallback)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalBooleanParam(
        "PublicApplicationConnectEnabled",
        req.publicApplicationConnectEnabled
      )
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Applications.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: ApplicationCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ApplicationCreateException, Application] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[ApplicationJsonRep](entity).map(j => j.toModelUnsafe)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ApplicationCreateException.Api =
    ApplicationCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ApplicationCreateException.Unspecified = ApplicationCreateException.Unspecified(msg, cause)
}
