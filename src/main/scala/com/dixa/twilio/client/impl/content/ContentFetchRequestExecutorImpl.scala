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

import com.dixa.twilio.client.content.ContentFetchRequestExecutor
import com.dixa.twilio.client.content.ContentFetchRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.content.ContentTemplate
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

private[impl] final class ContentFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentFetchRequest
  ): Either[ContentFetchException, HttpRequest] =
    createHttpRequestFor(s"/${ApiVersion.V1}/Content/${req.contentSid}", connSettings)

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ContentFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ContentFetchException, ContentTemplate] =
    httpResponse.status match {
      case StatusCodes.OK =>
        Try(ujson.read(entity.toString)) match {
          case Failure(ex)   => Left(ContentFetchException.Unspecified(ex))
          case Success(json) =>
            ContentJsonRep.parseContentTemplate(json).left.map(ContentFetchException.Unspecified(_))
        }
      case StatusCodes.NotFound =>
        Left(ContentFetchException.ContentNotFound(request.contentSid))
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
