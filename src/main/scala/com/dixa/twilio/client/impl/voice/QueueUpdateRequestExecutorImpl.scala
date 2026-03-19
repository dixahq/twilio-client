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
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.QueueUpdateRequestExecutor
import com.dixa.twilio.client.voice.QueueUpdateRequestExecutor.{
  QueueUpdateException,
  QueueUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Queue

import scala.concurrent.ExecutionContext

private[client] class QueueUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends QueueUpdateRequestExecutor {

  import QueueUpdateRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: QueueUpdateRequestExecutor.QueueUpdateRequest
  ): Either[QueueUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam(accountSidParamKey, req.accountSid)
      .withParam(sidParamKey, req.sid.toString)
      .withOptionalParam(friendlyNameParamKey, req.friendlyName)
      .withOptionalParam(maxSizeParamKey, req.maxSize)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Queues/${req.sid}.json",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)))
  }

  override protected def mapApiException(apiException: ApiException): QueueUpdateException.Api =
    QueueUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): QueueUpdateException.Unspecified = QueueUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: QueueUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[QueueUpdateException, Queue] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[QueueJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(req: QueueUpdateRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => QueueUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(QueueUpdateException.QueueNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              QueueUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}

private object QueueUpdateRequestExecutorImpl {
  private val accountSidParamKey   = "AccountSid"
  private val sidParamKey          = "Sid"
  private val friendlyNameParamKey = "FriendlyName"
  private val maxSizeParamKey      = "MaxSize"
}
