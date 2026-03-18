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

package com.dixa.twilio.client.impl.messaging

import org.apache.pekko.Done
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  ApiVersion,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString
}
import com.dixa.twilio.client.messaging.PhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[impl] final class PhoneNumberDeleteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberDeleteRequestExecutor {

  import PhoneNumberDeleteRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberDeleteRequest
  ): Either[PhoneNumberDeleteException, HttpRequest] =
    createHttpRequestFor(
      s"/${ApiVersion.V1}/Services/${req.serviceSid}/PhoneNumbers/${req.phoneNumberSid}",
      connSettings
    )

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    PhoneNumberDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = PhoneNumberDeleteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: PhoneNumberDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[PhoneNumberDeleteException, Done] = httpResponse.status match {
    case StatusCodes.OK | StatusCodes.NoContent =>
      Right(Done)
    case StatusCodes.NotFound =>
      buildResultForNotFoundResponse(entity)
    case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
  }

  private def buildResultForNotFoundResponse(entity: HttpEntityString) = {
    val msg = entity.parse[DefaultApiErrorEntityJsonRep]().fold(_.getMessage, _.message)
    Left(PhoneNumberDeleteException.NotFound(msg))
  }
}
