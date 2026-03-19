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

package com.dixa.twilio.client.impl.phonenumber

import org.apache.pekko.Done
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{
  HttpMethod,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[impl] class OutgoingCallerIdDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends OutgoingCallerIdDeleteRequestExecutor {

  override protected def parseHttpResponse(
      request: OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException, Done] = {
    httpResponse.status match {
      case StatusCodes.NoContent => Right(Done)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest
  ): Either[OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException, HttpRequest] =
    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/OutgoingCallerIds/${req.outGoingCallerId}.json",
      connSettings
    )

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdDeleteException.Api = OutgoingCallerIdDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): OutgoingCallerIdDeleteException.Unspecified =
    OutgoingCallerIdDeleteException.Unspecified(msg, cause)
}
