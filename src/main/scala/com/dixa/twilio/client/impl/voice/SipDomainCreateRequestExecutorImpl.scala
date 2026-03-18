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

package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{DefaultApiErrorEntityJsonRep, _}
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
      case StatusCodes.BadRequest =>
        parseEntityAs[DefaultApiErrorEntityJsonRep](entity) match {
          case Right(err) if err.code == 21231L || err.code == 21232L =>
            Left(SipDomainCreateException.InvalidDomainName(request.domainName))
          case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
        }
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
