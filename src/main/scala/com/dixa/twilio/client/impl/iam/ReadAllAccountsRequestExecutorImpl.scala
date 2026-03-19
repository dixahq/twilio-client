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

package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor
import com.dixa.twilio.client.iam.ReadAllAccountsRequestExecutor.{
  ReadAllAccountsException,
  ReadAllAccountsRequest
}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.client.impl.TwilioClientPickler.{macroR, Reader}

import scala.concurrent.ExecutionContext

private[impl] class ReadAllAccountsRequestExecutorImpl(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ReadAllAccountsRequestExecutor {
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET
  override def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ReadAllAccountsRequestExecutor.ReadAllAccountsRequest
  ): Either[ReadAllAccountsException, HttpRequest] = {
    val queryParams = QueryParamBuilder.empty
      .withParam("PageSize", "1000")
      .withOptionalParam("Status", req.status)
      .withOptionalParam("FriendlyName", req.name)
      .build
    createHttpRequestFor(s"/2010-04-01/Accounts.json$queryParams", connSettings)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    ReadAllAccountsException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = ReadAllAccountsException.Unspecified(msg, cause)

  private case class TwilioAccountsOuterJsonRep(accounts: Vector[TwilioAccountJsonRep])

  private implicit val twilioAccountsOuterJsonRepReader: Reader[TwilioAccountsOuterJsonRep] =
    macroR[TwilioAccountsOuterJsonRep]

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ReadAllAccountsRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ReadAllAccountsException, TwilioAccount]] = {
    responseEntity.parse[TwilioAccountsOuterJsonRep]() match {
      case Left(ex) =>
        List(
          Left(ReadAllAccountsException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause)))
        )
      case Right(decoded: TwilioAccountsOuterJsonRep) =>
        decoded.accounts.map { jsonRep =>
          Right(jsonRep.toModel)
        }.toList
    }
  }
}
