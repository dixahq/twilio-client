package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import akka.stream.Materializer
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchException
import com.dixa.twilio.client.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[iam] final class AccountFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccountFetchRequestExecutor {

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccountFetchRequestExecutor.AccountFetchRequest
  ): HttpRequest = ???

  override protected def mapApiException(apiException: ApiException): AccountFetchException.Api =
    AccountFetchException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): AccountFetchException.UnspecifiedError = AccountFetchException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AccountFetchRequestExecutor.AccountFetchRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntity.Strict
  ): Either[AccountFetchRequestExecutor.AccountFetchException, TwilioAccount] = ???
}
