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
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.phonenumber.IncomingPhoneNumberDeleteRequestExecutor
import com.dixa.twilio.client.phonenumber.IncomingPhoneNumberDeleteRequestExecutor.{
  IncomingPhoneNumberDeleteException,
  IncomingPhoneNumberDeleteRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}

import scala.concurrent.ExecutionContext

private[impl] class IncomingPhoneNumberDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends IncomingPhoneNumberDeleteRequestExecutor {

  override protected def parseHttpResponse(
      request: IncomingPhoneNumberDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[IncomingPhoneNumberDeleteException, Done] = {
    httpResponse.status match {
      case StatusCodes.NoContent | StatusCodes.OK => Right(Done)
      case StatusCodes.NotFound                   => buildResultForNotFoundResponse(request, entity)
      case StatusCodes.MethodNotAllowed           =>
        val moreInfo = parseEntityAs[DefaultApiErrorEntityJsonRep](entity).toOption
          .map(_.more_info)
        Left(
          IncomingPhoneNumberDeleteException.MethodNotAllowed(
            request.accountSid,
            request.phoneNumberId,
            moreInfo
          )
        )
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: IncomingPhoneNumberDeleteRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity)
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              IncomingPhoneNumberDeleteException.PhoneNumberNotFound(
                req.accountSid,
                req.phoneNumberId
              )
            )
          case other =>
            Left(
              IncomingPhoneNumberDeleteException.Unspecified(
                Some(
                  s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                    s"$other represent. Full error entity from Twilio: $entity"
                ),
                None
              )
            )
        }
      }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest
  ): Either[
    IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException,
    HttpRequest
  ] =
    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid.twilioString}/IncomingPhoneNumbers/${req.phoneNumberId.twilioString}.json",
      connSettings
    )

  override protected def mapApiException(
      apiException: ApiException
  ): IncomingPhoneNumberDeleteException.Api = IncomingPhoneNumberDeleteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): IncomingPhoneNumberDeleteException.Unspecified =
    IncomingPhoneNumberDeleteException.Unspecified(msg, cause)
}
