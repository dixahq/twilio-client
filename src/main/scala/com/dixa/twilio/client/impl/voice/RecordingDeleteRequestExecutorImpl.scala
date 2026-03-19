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
import com.dixa.twilio.client.voice.RecordingDeleteRequestExecutor
import com.dixa.twilio.client.voice.RecordingDeleteRequestExecutor.{
  RecordingDeleteRequest,
  RecordingDeleteRequestException
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[client] class RecordingDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends RecordingDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: RecordingDeleteRequest
  ): Either[RecordingDeleteRequestException, HttpRequest] = {
    createHttpRequestFor(
      s"""/${apiVersion.twilioString}/Accounts/${req.accountSid}/Recordings/${req.sid}.json""",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): RecordingDeleteRequestException.Api =
    RecordingDeleteRequestException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): RecordingDeleteRequestException.Unspecified =
    RecordingDeleteRequestException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: RecordingDeleteRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[RecordingDeleteRequestException, Unit] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(())
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(req, entity)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: RecordingDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => RecordingDeleteRequestException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              RecordingDeleteRequestException.RecordingNotFound(req.accountSid, req.sid)
            )
          case other =>
            Left(
              RecordingDeleteRequestException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
