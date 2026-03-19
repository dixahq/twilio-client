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

package com.dixa.twilio.client.impl.messaging

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor
import com.dixa.twilio.client.messaging.ServicesReadRequestExecutor.ServicesReadException
import com.dixa.twilio.client.{messaging, ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.concurrent.ExecutionContext

private[impl] class ServicesReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ServicesReadRequestExecutor {

  /** Specify the sub domain in twilio, that this API request is against. */
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  /** Specify the Http method that this API request uses */
  override protected def method: HttpMethod = HttpMethods.GET
  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: messaging.ServicesReadRequestExecutor.ServicesReadRequest
  ): Either[ServicesReadException, HttpRequest] =
    createHttpRequestFor(s"/${ApiVersion.V1}/Services?PageSize=1000", connSettings)

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ServicesReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ServicesReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(services: List[MessagingServiceJsonRep])

  private implicit val outerJsonRepReader: Reader[OuterJsonRep] =
    macroR[OuterJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ServicesReadRequestExecutor.ServicesReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ServicesReadException, TwilioMessagingService]] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ServicesReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.services.map { jsonRep =>
          Right(jsonRep.toTwilioMessagingService)
        }
    }
  }
}
