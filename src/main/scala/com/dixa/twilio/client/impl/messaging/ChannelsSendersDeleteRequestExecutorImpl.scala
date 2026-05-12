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

import com.dixa.twilio.client.ApiException.{BadRequestException, NotFound}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString}
import com.dixa.twilio.client.messaging.ChannelSendersException.Api
import com.dixa.twilio.client.messaging.{
  ChannelSendersException,
  ChannelsSendersDeleteRequestExecutor
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.FUnit
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

class ChannelsSendersDeleteRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ChannelsSendersDeleteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Messaging

  override protected def method: HttpMethod = HttpMethods.DELETE

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest
  ): Either[ChannelSendersException, HttpRequest] = {
    createHttpRequestFor(
      s"/${ApiVersion.V2}/Channels/Senders/${req.channelSenderSid.twilioString}",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      request: ChannelsSendersDeleteRequestExecutor.ChannelSenderDeleteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ChannelSendersException, FUnit] = {
    httpResponse.status match {
      case StatusCodes.NotFound                   => Left(Api(NotFound(entity.toString)))
      case StatusCodes.BadRequest                 => Left(Api(BadRequestException(entity.toString)))
      case StatusCodes.OK | StatusCodes.NoContent => Right(FUnit)
      case _ => Left(ChannelSendersException.Unexpected(Some(entity.toString), None))
    }
  }

  override protected def mapApiException(apiException: ApiException): ChannelSendersException.Api =
    ChannelSendersException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ChannelSendersException.Unspecified = ChannelSendersException.Unspecified(msg, cause)
}
