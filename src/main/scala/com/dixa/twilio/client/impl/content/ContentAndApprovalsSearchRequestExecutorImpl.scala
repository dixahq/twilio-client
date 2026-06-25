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

import com.dixa.twilio.client.content.ContentAndApprovalsSearchRequestExecutor
import com.dixa.twilio.client.content.ContentAndApprovalsSearchRequestExecutor._
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.content.ContentTemplateWithApproval
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

private[impl] final class ContentAndApprovalsSearchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ContentAndApprovalsSearchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Content
  override protected def method: HttpMethod      = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ContentAndApprovalsSearchRequest
  ): Either[ContentAndApprovalsSearchException, HttpRequest] = {
    val params =
      req.languages.map(l => s"Language=$l") ++
        req.contentTypes.map(ct => s"ContentType=$ct") ++
        req.channelEligibilities.map(ce =>
          s"ChannelEligibility=${ce.channel}:${ce.templateStatus}"
        ) ++
        req.content.map(c => s"Content=${java.net.URLEncoder.encode(c, "UTF-8")}") ++
        req.contentName.map(cn => s"ContentName=${java.net.URLEncoder.encode(cn, "UTF-8")}") ++
        req.dateCreatedBefore.map(d =>
          s"DateCreatedBefore=${DateTimeFormatter.ISO_INSTANT.format(d)}"
        ) ++
        req.dateCreatedAfter.map(d =>
          s"DateCreatedAfter=${DateTimeFormatter.ISO_INSTANT.format(d)}"
        ) ++
        req.pageSize.map(s => s"PageSize=$s")

    val queryString = if (params.isEmpty) "" else s"?${params.mkString("&")}"
    createHttpRequestFor(s"/${ApiVersion.V2}/ContentAndApprovals$queryString", connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ContentAndApprovalsSearchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ContentAndApprovalsSearchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ContentAndApprovalsSearchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ContentAndApprovalsSearchException, ContentTemplateWithApproval]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        Try(ujson.read(responseEntity.toString)) match {
          case Failure(ex) =>
            List(Left(ContentAndApprovalsSearchException.Unspecified(ex)))
          case Success(json) =>
            json("contents").arr.map { item =>
              ContentJsonRep
                .parseContentTemplateWithApproval(item)
                .left
                .map(ContentAndApprovalsSearchException.Unspecified(_))
            }.toList
        }
      case _ =>
        List(buildResultForUnhandledResponse(request, httpRequest, httpResponse, responseEntity))
    }
}
