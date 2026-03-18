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
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.CallRecordingCreateRequestExecutor
import com.dixa.twilio.client.voice.CallRecordingCreateRequestExecutor.CallRecordingCreateException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

private[client] class CallRecordingCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallRecordingCreateRequestExecutor {

  import CallRecordingCreateRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallRecordingCreateRequestExecutor.CallRecordingCreateRequest
  ): Either[CallRecordingCreateRequestExecutor.CallRecordingCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withCollectionParam(
        recordingStatusCallbackEventParamKey,
        req.recordingStatusCallbackEvent.getOrElse(Set.empty)
      )
      .withOptionalParam(recordingStatusCallbackParamKey, req.recordingStatusCallback)
      .withOptionalParam(recordingStatusCallbackMethodParamKey, req.recordingStatusCallbackMethod)
      .withOptionalParam(trimParamKey, req.trim)
      .withOptionalParam(recordingChannelsParamKey, req.recordingChannels)
      .withOptionalParam(recordingTrackParamKey, req.recordingTrack)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.callSid}/Recordings.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: CallRecordingCreateRequestExecutor.CallRecordingCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallRecordingCreateRequestExecutor.CallRecordingCreateException, Recording] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[RecordingJsonRep](entity).map(j => j.toModel)
      case StatusCodes.NotFound => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: CallRecordingCreateRequestExecutor.CallRecordingCreateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => CallRecordingCreateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(CallRecordingCreateException.CallNotFound(req.accountSid, req.callSid))
          case other =>
            Left(
              CallRecordingCreateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): CallRecordingCreateException.Api =
    CallRecordingCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallRecordingCreateException.Unspecified = CallRecordingCreateException.Unspecified(msg, cause)
}

private object CallRecordingCreateRequestExecutorImpl {
  private val recordingStatusCallbackEventParamKey  = "RecordingStatusCallbackEvent"
  private val recordingChannelsParamKey             = "RecordingChannels"
  private val recordingStatusCallbackParamKey       = "RecordingStatusCallback"
  private val recordingStatusCallbackMethodParamKey = "RecordingStatusCallbackMethod"
  private val recordingTrackParamKey                = "RecordingTrack"
  private val trimParamKey                          = "Trim"
}
