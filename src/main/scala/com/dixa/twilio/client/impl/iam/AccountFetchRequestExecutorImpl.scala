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

package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchException
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[iam] final class AccountFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccountFetchRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccountFetchRequestExecutor.AccountFetchRequest
  ): Either[AccountFetchException, HttpRequest] =
    createHttpRequestFor(s"/2010-04-01/Accounts/${req.accountSid}.json", connSettings)

  override protected def mapApiException(apiException: ApiException): AccountFetchException.Api =
    AccountFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AccountFetchException.UnspecifiedError = AccountFetchException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AccountFetchRequestExecutor.AccountFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AccountFetchRequestExecutor.AccountFetchException, TwilioAccount] =
    httpResponse.status match {
      case StatusCodes.OK => parseEntityAs[TwilioAccountJsonRep](entity).map(_.toModel)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
