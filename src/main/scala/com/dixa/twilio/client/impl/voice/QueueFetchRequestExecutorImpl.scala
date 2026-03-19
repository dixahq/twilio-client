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
import com.dixa.twilio.client.voice.QueueFetchRequestExecutor
import com.dixa.twilio.client.voice.QueueFetchRequestExecutor.{
  QueueFetchException,
  QueueFetchRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Queue

import scala.concurrent.ExecutionContext

private[client] class QueueFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends QueueFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: QueueFetchRequestExecutor.QueueFetchRequest
  ): Either[QueueFetchException, HttpRequest] = {
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Queues/${req.sid}.json",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): QueueFetchException.Api =
    QueueFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): QueueFetchException.Unspecified = QueueFetchException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: QueueFetchRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[QueueFetchException, Queue] = httpResponse.status match {
    case StatusCodes.OK       => parseEntityAs[QueueJsonRep](entity).map(_.toModel)
    case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
    case _                    => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(req: QueueFetchRequest, entity: HttpEntityString) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => QueueFetchException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(QueueFetchException.QueueNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              QueueFetchException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represents. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
