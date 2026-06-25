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

import com.dixa.twilio.client.content.ContentReadRequestExecutor
import com.dixa.twilio.client.content.ContentReadRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.content.ContentTemplate
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

private[impl] final class ContentReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentReadRequest
  ): Either[ContentReadException, HttpRequest] = {
    val queryString = req.pageSize.map(s => s"?PageSize=$s").getOrElse("")
    createHttpRequestFor(s"/${ApiVersion.V1}/Content$queryString", connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ContentReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ContentReadException, ContentTemplate]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        Try(ujson.read(responseEntity.toString)) match {
          case Failure(ex) =>
            List(Left(ContentReadException.Unspecified(ex)))
          case Success(json) =>
            json("contents").arr.map { item =>
              ContentJsonRep
                .parseContentTemplate(item)
                .left
                .map(ContentReadException.Unspecified(_))
            }.toList
        }
      case _ =>
        List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, responseEntity))
    }
}
