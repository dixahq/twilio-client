package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.iam.SecondaryAuthTokenCreateRequestExecutor
import com.dixa.twilio.client.iam.SecondaryAuthTokenCreateRequestExecutor.{
  SecondaryAuthTokenCreateException,
  SecondaryAuthTokenCreateRequest
}
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.AuthToken
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[iam] final class SecondaryAuthTokenCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends SecondaryAuthTokenCreateRequestExecutor {

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SecondaryAuthTokenCreateRequest
  ): Either[SecondaryAuthTokenCreateException, HttpRequest] = {
    Right(
      TwilioPath(
        ApiSubDomain.Accounts,
        HttpMethods.POST,
        s"/v1/AuthTokens/Secondary"
      )
        .createHttpRequest(connSettings)
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): SecondaryAuthTokenCreateException.Api =
    SecondaryAuthTokenCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): SecondaryAuthTokenCreateRequestExecutor.SecondaryAuthTokenCreateException.UnspecifiedError =
    SecondaryAuthTokenCreateException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: SecondaryAuthTokenCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[SecondaryAuthTokenCreateException, AuthToken.AuthTokenAndMetaData[
    AuthToken.Secondary
  ]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[SecondaryAuthTokenCreateRespJsonRep](entity).map(_.toModel)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
}
