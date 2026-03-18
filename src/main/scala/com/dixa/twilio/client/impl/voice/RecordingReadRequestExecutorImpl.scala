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
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.RecordingReadRequestExecutor
import com.dixa.twilio.client.voice.RecordingReadRequestExecutor.RecordingReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

class RecordingReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends RecordingReadRequestExecutor {

  import RecordingReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: RecordingReadRequestExecutor.RecordingReadRequest
  ): Either[
    RecordingReadRequestExecutor.RecordingReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam(callSidParamKey, req.callSid)
      .withOptionalParam(conferenceParamKey, req.conferenceSid)
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalBooleanParam(includeSoftDeletedParamKey, req.includeSoftDeleted)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Recordings.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): RecordingReadException.Api =
    RecordingReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): RecordingReadException.Unspecified =
    RecordingReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: RecordingReadRequestExecutor.RecordingReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    RecordingReadRequestExecutor.RecordingReadException,
    Recording
  ]] = {
    responseEntity.parse[RecordingListJsonRep]() match {
      case Left(parsingException: HttpEntityString.JsonParsingException) =>
        List(
          Left(
            RecordingReadException.ResponseParsingFailed(
              parsingException.entity.toString,
              parsingException.getMessage,
              Some(parsingException)
            )
          )
        )
      case Left(ex) =>
        List(
          Left(
            RecordingReadException.Unspecified(
              Some(ex.getMessage),
              Some(ex)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.recordings.map { _.toModel }.map { Right(_) }
    }

  }

}

private object RecordingReadRequestExecutorImpl {
  private val callSidParamKey            = "CallSid"
  private val conferenceParamKey         = "ConferenceSid"
  private val dateCreatedParamKey        = "DateCreated"
  private val includeSoftDeletedParamKey = "IncludeSoftDeleted"
}
