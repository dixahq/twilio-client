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

package com.dixa.twilio.client.impl.general

import org.apache.pekko.Done
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.general.UsageTriggerDeleteRequestExecutor
import com.dixa.twilio.client.general.UsageTriggerDeleteRequestExecutor.{
  UsageTriggerDeleteException,
  UsageTriggerDeleteRequest
}
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[general] final class UsageTriggerDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends UsageTriggerDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: UsageTriggerDeleteRequest
  ): Either[UsageTriggerDeleteException, HttpRequest] = {
    createHttpRequestFor(
      s"/$apiVersion/Accounts/${req.accountSid.twilioString}/Usage/Triggers/${req.sid.twilioString}.json",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): UsageTriggerDeleteException.Api =
    UsageTriggerDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UsageTriggerDeleteException.UnspecifiedError =
    UsageTriggerDeleteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: UsageTriggerDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[UsageTriggerDeleteException, Done] =
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Done)
      case StatusCodes.NotFound  => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForNotFoundResponse(
      request: UsageTriggerDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => createUnspecifiedException("Error parsing entity for 404 response", e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            // Twilio returns this if you do not have the API enabled, and as there is no
            // variables in the path, it should be safe to assume that it's the ony thing
            // this code can mean for this API call.
            Left(
              UsageTriggerDeleteException
                .UsageTriggerNotFoundOnAccountException(request.accountSid, request.sid)
            )
          case other =>
            Left(
              createUnspecifiedException(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
