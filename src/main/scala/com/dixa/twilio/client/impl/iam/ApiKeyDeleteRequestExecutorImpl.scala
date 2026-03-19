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

package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.ApiKeyDeleteRequestExecutor
import com.dixa.twilio.client.iam.ApiKeyDeleteRequestExecutor.{KeyDeleteException, KeyDeleteRequest}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit

import scala.concurrent.ExecutionContext

private[client] class ApiKeyDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ApiKeyDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Iam

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: KeyDeleteRequest
  ): Either[KeyDeleteException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam("AccountSid", req.accountSid.toString)
      .build

    createHttpRequestFor(s"/v1/Keys/${req.sid}$params", connSettings)
  }

  override protected def mapApiException(
      apiException: ApiException
  ): KeyDeleteException.Api = KeyDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): KeyDeleteException.Unspecified = KeyDeleteException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: KeyDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[KeyDeleteException, FUnit] =
    httpResponse.status match {
      case StatusCodes.NoContent => Right(FUnit)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
