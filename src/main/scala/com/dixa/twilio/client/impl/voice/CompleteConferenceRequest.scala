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
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.voice.ConferenceJsonRep.TwilioConferenceJsonResp
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, TwilioUri}
import com.dixa.twilio.model.voice.Conference

import scala.concurrent.{ExecutionContext, Future}

private[impl] object CompleteConferenceRequest {

  def apply(
      connSettings: TwilioConnectionSettings,
      conference: Conference
  )(
      implicit http: HttpExt,
      materializer: Materializer,
      executionContext: ExecutionContext,
      apiVersion: ApiVersion
  ): Future[Conference] = {
    val req = TwilioUri
      .createPathUnsafe(
        ApiSubDomain.Api,
        HttpMethods.POST,
        s"/${apiVersion.twilioString}/Accounts/${conference.accountSid}/Conferences/${conference.sid}.json"
      )
      .createHttpRequestUnsafe(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, "Status=completed"))
    http.singleRequest(req).flatMap { resp =>
      if (resp.status != StatusCodes.OK) {
        throw new IllegalStateException(
          s"Could not close conference: $conference, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parse[TwilioConferenceJsonResp]().toTry.get
        decoded.toModel
      }
    }
  }
}
