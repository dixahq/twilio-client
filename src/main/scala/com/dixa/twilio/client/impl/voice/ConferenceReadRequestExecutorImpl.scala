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

package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor.ConferenceReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Conference

import scala.concurrent.ExecutionContext

class ConferenceReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ConferenceReadRequestExecutor {

  import ConferenceReadRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceReadRequestExecutor.ConferenceReadRequest
  ): Either[ConferenceReadRequestExecutor.ConferenceReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalDateParam(dateUpdatedParamKey, req.dateUpdated)
      .withOptionalParam(friendlyNameParamKey, req.friendlyName)
      .withOptionalParam(statusParamKey, req.status)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Conferences.json$params",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): ConferenceReadException.Api =
    ConferenceReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceReadException.Unspecified = ConferenceReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ConferenceReadRequestExecutor.ConferenceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ConferenceReadRequestExecutor.ConferenceReadException, Conference]] = {
    responseEntity.parse[ConferenceListJsonRep]() match {
      case Left(ex) =>
        List(Left(ConferenceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(listJsonRep) => listJsonRep.conferences.map { _.toModel }.map { Right(_) }
    }

  }

}

private object ConferenceReadRequestExecutorImpl {
  private val dateCreatedParamKey  = "DateCreated"
  private val dateUpdatedParamKey  = "DateUpdated"
  private val friendlyNameParamKey = "FriendlyName"
  private val statusParamKey       = "Status"
}
