package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.SipDomainCreateRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.SipDomain

import scala.concurrent.ExecutionContext

private[client] class SipDomainCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends SipDomainCreateRequestExecutor {

  import com.dixa.twilio.client.voice.SipDomainCreateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SipDomainCreateRequest
  ): Either[SipDomainCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid)
      .withParam("DomainName", req.domainName)
      .withOptionalParam("FriendlyName", req.friendlyName)
      .withOptionalParam("VoiceUrl", req.voiceUrl)
      .withOptionalParam("VoiceMethod", req.voiceMethod)
      .withOptionalParam("VoiceFallbackUrl", req.voiceFallbackUrl)
      .withOptionalParam("VoiceFallbackMethod", req.voiceFallbackMethod)
      .withOptionalParam("VoiceStatusCallbackUrl", req.voiceStatusCallbackUrl)
      .withOptionalParam("VoiceStatusCallbackMethod", req.voiceStatusCallbackMethod)
      .withOptionalBooleanParam("SipRegistration", req.sipRegistration)
      .withOptionalBooleanParam("EmergencyCallingEnabled", req.emergencyCallingEnabled)
      .withOptionalBooleanParam("Secure", req.secure)
      .withOptionalParam("ByocTrunkSid", req.byocTrunkSid)
      .withOptionalParam("EmergencyCallerSid", req.emergencyCallerSid)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/SIP/Domains.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: SipDomainCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[SipDomainCreateException, SipDomain] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[SipDomainJsonRep](entity).map(j => j.toModelUnsafe)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SipDomainCreateException.Api =
    SipDomainCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): SipDomainCreateException.Unspecified = SipDomainCreateException.Unspecified(msg, cause)
}
