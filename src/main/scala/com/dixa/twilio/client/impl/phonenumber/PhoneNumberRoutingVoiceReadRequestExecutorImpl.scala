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

package com.dixa.twilio.client.impl.phonenumber

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.phonenumber.PhoneNumberRoutingVoiceReadRequestExecutor
import com.dixa.twilio.client.phonenumber.PhoneNumberRoutingVoiceReadRequestExecutor._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{PhoneNumberE164, PhoneNumberRoutingVoice}

import scala.concurrent.ExecutionContext

private[impl] final class PhoneNumberRoutingVoiceReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberRoutingVoiceReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Routes

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberRoutingVoiceReadRequest
  ): Either[PhoneNumberRoutingVoiceReadException, HttpRequest] =
    createHttpRequestFor(
      s"/${ApiVersion.V2}/PhoneNumbers/${req.phoneNumber.asString}",
      connSettings
    )

  override protected def mapApiException(
      apiException: ApiException
  ): PhoneNumberRoutingVoiceReadException.Api =
    PhoneNumberRoutingVoiceReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): PhoneNumberRoutingVoiceReadException.Unspecified =
    PhoneNumberRoutingVoiceReadException.Unspecified(msg, cause)

  private case class PhoneNumberRoutingRegionJsonRep(
      sid: String,
      account_sid: String,
      phone_number: String,
      friendly_name: String,
      voice_region: String
  ) {
    def toModel: PhoneNumberRoutingVoice = PhoneNumberRoutingVoice(
      sid = PhoneNumberRoutingVoice.Sid.unsafe(sid),
      accountSid = TwilioAccount.Sid.unsafe(account_sid),
      phoneNumber = PhoneNumberE164.unsafe(phone_number),
      friendlyName = friendly_name,
      voiceRegion = voice_region
    )
  }

  private implicit val jsonRepReader: Reader[PhoneNumberRoutingRegionJsonRep] =
    macroR[PhoneNumberRoutingRegionJsonRep]

  override protected def parseHttpResponse(
      request: PhoneNumberRoutingVoiceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[PhoneNumberRoutingVoiceReadException, PhoneNumberRoutingVoice] =
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[PhoneNumberRoutingRegionJsonRep](entity)
          .map(_.toModel)
          .left
          .map(e => PhoneNumberRoutingVoiceReadException.Unspecified(Some(e.getMessage), Some(e)))
      case StatusCodes.NotFound =>
        Left(PhoneNumberRoutingVoiceReadException.PhoneNumberNotFound(request.phoneNumber))
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
