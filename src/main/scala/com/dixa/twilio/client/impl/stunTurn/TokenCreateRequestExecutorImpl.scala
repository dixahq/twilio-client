package com.dixa.twilio.client.impl.stunTurn

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.general.TokenCreateRequestExecutor
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.stunTurn.TokenCreateRequestExecutor
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.general.Token
import com.dixa.twilio.model.stunTurn.Token

import scala.concurrent.ExecutionContext

private[client] class TokenCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends TokenCreateRequestExecutor {

  import TokenCreateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: TokenCreateRequest
  ): Either[TokenCreateException, HttpRequest] = {

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Tokens.json",
      connSettings
    )
  }

  override protected def parseHttpResponse(
      request: TokenCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[TokenCreateException, Token] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[TokenJsonRep](entity).map(j => j.toModel)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): TokenCreateException.Api =
    TokenCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): TokenCreateException.Unspecified = TokenCreateException.Unspecified(msg, cause)
}
