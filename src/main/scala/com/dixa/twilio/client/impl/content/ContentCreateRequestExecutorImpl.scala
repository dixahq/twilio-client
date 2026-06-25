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

import com.dixa.twilio.client.content.ContentCreateRequestExecutor
import com.dixa.twilio.client.content.ContentCreateRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.content.ContentTemplate
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

private[impl] final class ContentCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentCreateRequest
  ): Either[ContentCreateException, HttpRequest] = {
    val typesJson = ujson.Obj.from(req.types.map { case (k, v) =>
      k -> ContentJsonRep.contentTypeToJson(v)
    })
    val body = ujson.Obj(
      "friendly_name" -> req.friendlyName,
      "language"      -> req.language,
      "variables"     -> ujson.Obj.from(req.variables.map { case (k, v) => k -> ujson.Str(v) }),
      "types"         -> typesJson
    )
    createHttpRequestFor(s"/${ApiVersion.V1}/Content", connSettings)
      .map(_.withEntity(HttpEntity(ContentTypes.`application/json`, ujson.write(body))))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentCreateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ContentCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ContentCreateException, ContentTemplate] =
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        Try(ujson.read(entity.toString)) match {
          case Failure(ex)   => Left(ContentCreateException.Unspecified(ex))
          case Success(json) =>
            ContentJsonRep
              .parseContentTemplate(json)
              .left
              .map(ContentCreateException.Unspecified(_))
        }
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
