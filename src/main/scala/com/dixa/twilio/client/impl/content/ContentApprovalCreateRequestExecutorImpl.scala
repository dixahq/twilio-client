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

import com.dixa.twilio.client.content.ContentApprovalCreateRequestExecutor
import com.dixa.twilio.client.content.ContentApprovalCreateRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.content.ContentApproval
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

private[impl] final class ContentApprovalCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentApprovalCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentApprovalCreateRequest
  ): Either[ContentApprovalCreateException, HttpRequest] = {
    val body = ujson.Obj(
      "name"     -> req.name,
      "category" -> req.category.twilioString
    )
    createHttpRequestFor(
      s"/${ApiVersion.V1}/Content/${req.contentSid}/ApprovalRequests/whatsapp",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/json`, ujson.write(body))))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentApprovalCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentApprovalCreateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      request: ContentApprovalCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ContentApprovalCreateException, ContentApproval] =
    httpResponse.status match {
      case StatusCodes.OK | StatusCodes.Created =>
        Try(ujson.read(entity.toString)) match {
          case Failure(ex)   => Left(ContentApprovalCreateException.Unspecified(ex))
          case Success(json) =>
            val status = ContentApproval.ApprovalStatus.values
              .find(_.twilioString == json.obj.get("status").map(_.str).getOrElse(""))
              .getOrElse(ContentApproval.ApprovalStatus.Received)
            val rejectionReason = json.obj.get("rejection_reason").flatMap {
              case ujson.Null => None
              case r          => if (r.str.isEmpty) None else Some(r.str)
            }
            val whatsapp = ContentApproval.WhatsappApproval(
              name = json("name").str,
              category = json("category").str,
              contentType = json.obj.get("content_type").map(_.str).getOrElse(""),
              status = status,
              rejectionReason = rejectionReason,
              allowCategoryChange = false
            )
            Right(
              ContentApproval(
                sid = request.contentSid,
                accountSid = None,
                whatsapp = Some(whatsapp)
              )
            )
        }
      case StatusCodes.NotFound =>
        Left(ContentApprovalCreateException.ContentNotFound(request.contentSid))
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
