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

import com.dixa.twilio.client.ApiException.{BadRequestException, NotFound, Unspecified}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.Uri.Query
import org.apache.pekko.http.scaladsl.model.{
  HttpMethod,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.{
  OutgoingCallerIdReadException,
  OutgoingCallerIdReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.phonenumber.OutgoingCallerId
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.concurrent.ExecutionContext

private[impl] class OutgoingCallerIdReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends OutgoingCallerIdReadRequestExecutor {

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: OutgoingCallerIdReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[
    Either[OutgoingCallerIdReadException, OutgoingCallerId]
  ] = {
    httpResponse.status match {
      case StatusCodes.BadRequest =>
        List(Left(OutgoingCallerIdReadException.Api(BadRequestException(responseEntity.toString))))
      case StatusCodes.NotFound =>
        List(Left(OutgoingCallerIdReadException.Api(NotFound(responseEntity.toString))))
      case StatusCodes.OK => parseBody(responseEntity)
      case _ => List(Left(OutgoingCallerIdReadException.Api(Unspecified(responseEntity.toString))))
    }
  }

  private def parseBody(entity: HttpEntityString) = {
    entity.parse[OuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(OutgoingCallerIdReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: OuterJsonRep) =>
        decoded.outgoing_caller_ids.map { jsonReq =>
          Right(jsonReq.toModel)
        }
    }
  }

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest
  ): Either[OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException, HttpRequest] = {
    val query = {
      val friendlyNameQuery: Option[(String, String)] = req.filter.friendlyName.map {
        friendlyName =>
          "FriendlyName" -> friendlyName.toString
      }
      val phonenumberQuery: Option[(String, String)] = req.filter.phoneNumber.map { phoneNumber =>
        "PhoneNumber" -> phoneNumber.asString
      }
      Query(
        Map("PageSize" -> req.filter.pageSize.toString) ++
          List(
            friendlyNameQuery,
            phonenumberQuery
          ).flatten.toMap
      )
    }

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/OutgoingCallerIds.json?${query.toString}",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): OutgoingCallerIdReadException.Api = OutgoingCallerIdReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): OutgoingCallerIdReadException.Unspecified =
    OutgoingCallerIdReadException.Unspecified(msg, cause)

  private case class OuterJsonRep(outgoing_caller_ids: List[OutgoingCallerIdJsonRep])

  private implicit val outerJsonRepReader: Reader[OuterJsonRep] =
    macroR[OuterJsonRep]

}
