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

import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}
import com.dixa.twilio.client.messaging.{
  ChannelsSendersCommonExceptions,
  ChannelsSendersListRequestExecutor
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

private[impl] class ChannelsSendersListRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelsSendersListRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: org.apache.pekko.http.scaladsl.model.HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelsSendersListRequestExecutor.ChannelSendersListRequest
  ): Either[ChannelsSendersCommonExceptions, HttpRequest] = {
    val optionalParams = List(
      req.pageSize.map(size => s"PageSize=$size")
    ).flatten

    val allParams   = s"Channel=${req.channel.value}" :: optionalParams
    val queryString = s"?${allParams.mkString("&")}"
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders$queryString",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ChannelsSendersCommonExceptions.Api =
    ChannelsSendersCommonExceptions.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelsSendersCommonExceptions.Unspecified = ChannelsSendersCommonExceptions.Unspecified(msg, cause)

  private case class SendersListJsonRep(senders: List[ChannelsSendersJsonRep])

  private implicit val sendersListJsonRepReader: Reader[SendersListJsonRep] =
    macroR[SendersListJsonRep]

  override protected def parseHttpResponse(
      request: ChannelsSendersListRequestExecutor.ChannelSendersListRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[
    ChannelsSendersCommonExceptions,
    ChannelsSendersListRequestExecutor.ChannelSendersListResponse
  ] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        entity.parse[SendersListJsonRep]() match {
          case Left(ex) =>
            Left(ChannelsSendersCommonExceptions.ParseFailure(ex.cause.getMessage))
          case Right(decoded) =>
            val parsedSenders = decoded.senders.flatMap { jsonRep =>
              ChannelsSendersJsonRep.toModelOldExceptionHandling(jsonRep).toOption
            }
            Right(ChannelsSendersListRequestExecutor.ChannelSendersListResponse(parsedSenders))
        }
      case _ =>
        buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }
}
