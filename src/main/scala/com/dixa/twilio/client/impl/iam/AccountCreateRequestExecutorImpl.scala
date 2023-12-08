package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.AccountCreateRequestExecutor
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[client] class AccountCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends AccountCreateRequestExecutor {

  import AccountCreateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccountCreateRequest
  ): Either[AccountCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam("FriendlyName", req.friendlyName)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: AccountCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AccountCreateException, TwilioAccount] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[TwilioAccountJsonRep](entity).map(j => j.toModel)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(
      apiException: ApiException
  ): AccountCreateException.Api =
    AccountCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AccountCreateException.Unspecified = AccountCreateException.Unspecified(msg, cause)
}
