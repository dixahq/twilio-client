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
import com.dixa.twilio.client.impl.Formatter.dateTime
import com.dixa.twilio.client.impl.messaging.MediaResourceUrlFactory.buildMediaResourcePath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor
import com.dixa.twilio.client.messaging.MessageMediaResourceReadRequestExecutor.{
  MessageMediaResourceReadException,
  MessageMediaResourceReadRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{Media, MediaResourceReference, Message}
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.util.Try

private[impl] class MessageMediaResourceReadRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends MessageMediaResourceReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: MessageMediaResourceReadRequestExecutor.MessageMediaResourceReadRequest
  ): Either[MessageMediaResourceReadException, HttpRequest] = {
    val requestPath = buildMediaResourcePath(req.accountSid, req.messageSid)
    createHttpRequestFor(requestPath, connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    MessageMediaResourceReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = MessageMediaResourceReadException.Unspecified(msg, cause)

  private case class MediaResourcesReferenceJsonRep(
      sid: String,
      account_sid: String,
      parent_sid: String,
      content_type: String,
      date_created: String,
      date_updated: String,
      uri: String
  ) {
    def toModel(
        messageSid: Message.Sid,
        connSettings: TwilioConnectionSettings
    ): MediaResourceReference = {
      val accountSid = TwilioAccount.Sid.unsafe(account_sid)
      val mediaSid   = Media.Sid.unsafe(sid)
      MediaResourceReference(
        sid = mediaSid,
        accountSid = accountSid,
        parentSid = Message.Sid.unsafe(parent_sid),
        contentType = content_type,
        dateCreated = Try(Instant.from(dateTime.parse(date_created))).getOrElse(Instant.now),
        dateUpdated = Try(Instant.from(dateTime.parse(date_updated))).getOrElse(Instant.now),
        MediaResourceUrlFactory.resourceUrl(accountSid, messageSid, mediaSid, connSettings)
      )
    }
  }

  private implicit val mediaResourcesReferenceJsonRepReader
      : Reader[MediaResourcesReferenceJsonRep] =
    macroR[MediaResourcesReferenceJsonRep]

  private case class MediaResourceListJsonRep(
      first_page_uri: String,
      end: Int,
      media_list: List[MediaResourcesReferenceJsonRep],
      previous_page_uri: Option[String] = None,
      uri: String,
      page_size: Int,
      start: Int,
      next_page_uri: Option[String] = None,
      page: Int
  )

  private implicit val mediaResourceListJsonRepReader: Reader[MediaResourceListJsonRep] =
    macroR[MediaResourceListJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: MessageMediaResourceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[MessageMediaResourceReadException, MediaResourceReference]] = {
    responseEntity.parse[MediaResourceListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            MessageMediaResourceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))
          )
        )
      case Right(decoded: MediaResourceListJsonRep) =>
        decoded.media_list.map { jsonRep =>
          Right(jsonRep.toModel(request.messageSid, connectionSettings))
        }
    }
  }
}
