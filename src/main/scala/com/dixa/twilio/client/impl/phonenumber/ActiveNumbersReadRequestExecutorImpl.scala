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

package com.dixa.twilio.client.impl.phonenumber

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor
import com.dixa.twilio.client.phonenumber.ActiveNumbersReadRequestExecutor.{
  ActiveNumbersReadException,
  ActiveNumbersReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.TwilioActivePhoneNumber
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.concurrent.ExecutionContext

private[impl] class ActiveNumbersReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ActiveNumbersReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Preview

  override protected def method: HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ActiveNumbersReadRequestExecutor.ActiveNumbersReadRequest
  ): Either[ActiveNumbersReadException, HttpRequest] = {
    createHttpRequestFor(
      s"/Numbers/ActiveNumbers/${req.phoneNumberSid.map(_.toString).getOrElse("")}?PageSize=1000",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ActiveNumbersReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ActiveNumbersReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(items: List[ActivePhoneNumberJsonRep])

  private implicit val outerJsonRepReader: Reader[OuterJsonRep] =
    macroR[OuterJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ActiveNumbersReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ActiveNumbersReadException, TwilioActivePhoneNumber]] = {
    responseEntity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ActiveNumbersReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.items.map { jsonRep =>
          Right(jsonRep.toModel)
        }
    }
  }
}
