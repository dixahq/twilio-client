// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
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
import com.dixa.twilio.client
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.RecordingFetchRequestExecutor
import com.dixa.twilio.client.voice.RecordingFetchRequestExecutor.RecordingFetchException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

private[client] class RecordingFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends RecordingFetchRequestExecutor {

  import RecordingFetchRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: RecordingFetchRequestExecutor.RecordingFetchRequest
  ): Either[RecordingFetchRequestExecutor.RecordingFetchException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalBooleanParam(includeSoftDeletedParamKey, req.includeSoftDeleted)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Recordings/${req.sid}.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: RecordingFetchRequestExecutor.RecordingFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[client.voice.RecordingFetchRequestExecutor.RecordingFetchException, Recording] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[RecordingJsonRep](entity).map(j => j.toModel)
      case StatusCodes.NotFound => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: RecordingFetchRequestExecutor.RecordingFetchRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => RecordingFetchException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(RecordingFetchException.RecordingNotFound(req.accountSid, req.sid))
          case other =>
            Left(
              RecordingFetchException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }

  override protected def mapApiException(apiException: ApiException): RecordingFetchException.Api =
    RecordingFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): RecordingFetchException.Unspecified = RecordingFetchException.Unspecified(msg, cause)
}

private object RecordingFetchRequestExecutorImpl {
  private val includeSoftDeletedParamKey = "IncludeSoftDeleted"
}
