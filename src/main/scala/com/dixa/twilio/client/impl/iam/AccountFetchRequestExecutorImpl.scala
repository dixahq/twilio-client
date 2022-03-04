package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor
import com.dixa.twilio.client.iam.AccountFetchRequestExecutor.AccountFetchException
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import io.circe.generic.auto._
import scala.concurrent.ExecutionContext

private[iam] final class AccountFetchRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccountFetchRequestExecutor {

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccountFetchRequestExecutor.AccountFetchRequest
  ): HttpRequest = {
    TwilioPath(
      ApiSubDomain.Api,
      HttpMethods.GET,
      s"/2010-04-01/Accounts/${req.accountSid}.json"
    )
      .createHttpRequest(connSettings)
  }

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
  ): Either[AccountFetchRequestExecutor.AccountFetchException, TwilioAccount] =
    httpResponse.status match {
      case StatusCodes.OK => buildSuccessResponse(entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildSuccessResponse(entity: HttpEntity.Strict) = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[TwilioAccountJsonRep]()
    Right(decoded.toModel)
  }
}
