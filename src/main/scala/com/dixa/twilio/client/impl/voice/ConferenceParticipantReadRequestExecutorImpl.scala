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
import com.dixa.twilio.client.impl.voice.ConferenceParticipantReadRequestExecutorImpl.{
  coachingParamKey,
  holdParamKey,
  mutedParamKey
}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.ConferenceParticipantReadRequestExecutor
import com.dixa.twilio.client.voice.ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Conference

import scala.concurrent.ExecutionContext

class ConferenceParticipantReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ConferenceParticipantReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest
  ): Either[
    ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadException,
    HttpRequest
  ] = {
    val params = QueryParamBuilder.empty
      .withOptionalBooleanParam(mutedParamKey, req.muted)
      .withOptionalBooleanParam(holdParamKey, req.hold)
      .withOptionalBooleanParam(coachingParamKey, req.coaching)
      .build

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Conferences/${req.conferenceSid}/Participants.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ConferenceParticipantsReadException.Api =
    ConferenceParticipantsReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceParticipantsReadException.Unspecified =
    ConferenceParticipantsReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[
    ConferenceParticipantReadRequestExecutor.ConferenceParticipantsReadException,
    Conference.Participant
  ]] = {
    responseEntity.parse[ParticipantListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            ConferenceParticipantsReadException.Unspecified(
              Some(ex.cause.getMessage),
              Some(ex.cause)
            )
          )
        )
      case Right(listJsonRep) => listJsonRep.participants.map { _.toModel }.map { Right(_) }
    }

  }

}

private object ConferenceParticipantReadRequestExecutorImpl {
  private val mutedParamKey    = "Muted"
  private val holdParamKey     = "Hold"
  private val coachingParamKey = "Coaching"
}
