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

package com.dixa.twilio.client.impl.content

import com.dixa.twilio.client.content.ContentDeleteRequestExecutor
import com.dixa.twilio.client.content.ContentDeleteRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] final class ContentDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentDeleteRequest
  ): Either[ContentDeleteException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalBooleanParam("deleteInWaba", req.deleteInWaba)
    createHttpRequestFor(
      s"/${ApiVersion.V1}/Content/${req.contentSid}${params.build}",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentDeleteException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ContentDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ContentDeleteException, FUnit] =
    httpResponse.status match {
      case StatusCodes.NoContent | StatusCodes.OK => Right(FUnit)
      case StatusCodes.NotFound => Left(ContentDeleteException.ContentNotFound(request.contentSid))
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
