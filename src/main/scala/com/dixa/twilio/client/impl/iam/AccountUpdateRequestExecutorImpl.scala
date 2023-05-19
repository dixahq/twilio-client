package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.iam.AccountUpdateRequestExecutor
import com.dixa.twilio.client.impl.iam.AccountUpdateRequestExecutorImpl.{
  friendlyNameParamKey,
  sidParamKey,
  statusParamKey
}
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  DefaultApiErrorEntityJsonRep,
  HttpEntityString,
  QueryParamBuilder
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.TwilioAccount

import scala.concurrent.ExecutionContext

private[iam] final class AccountUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AccountUpdateRequestExecutor {

  import AccountUpdateRequestExecutor._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AccountUpdateRequest
  ): Either[AccountUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam(sidParamKey, req.accountSid.twilioString)
      .withOptionalParam(friendlyNameParamKey, req.friendlyName)
      .withOptionalParam(statusParamKey, req.status)
    createHttpRequestFor(s"/2010-04-01/Accounts/${req.accountSid}.json", connSettings)
      .map(
        _.withMethod(HttpMethods.POST).withEntity(
          HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params.buildForPostParams)
        )
      )
  }

  override protected def mapApiException(apiException: ApiException): AccountUpdateException.Api =
    AccountUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AccountUpdateException.UnspecifiedError = AccountUpdateException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AccountUpdateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AccountUpdateException, TwilioAccount] =
    httpResponse.status match {
      case StatusCodes.OK         => parseEntityAs[TwilioAccountJsonRep](entity).map(_.toModel)
      case StatusCodes.BadRequest => buildResultForBadRequestResponse(request, entity)
      case StatusCodes.NotFound   => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForBadRequestResponse(
      req: AccountUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => AccountUpdateException.UnspecifiedError(None, Some(e)))
      .flatMap { decoded =>
        decoded.code match {
          case 21479L =>
            Left(AccountUpdateException.ClosedAccountCannotBeReopened(req.accountSid))
          case other =>
            Left(
              AccountUpdateException.UnspecifiedError(
                Some(
                  s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                    s"$other represent. Full error entity from Twilio: $entity"
                ),
                None
              )
            )
        }
      }
  }

  private def buildResultForNotFoundResponse(
      req: AccountUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => AccountUpdateException.UnspecifiedError(None, Some(e)))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(AccountUpdateException.AccountNotFound(req.accountSid))
          case other =>
            Left(
              AccountUpdateException.UnspecifiedError(
                Some(
                  s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                    s"$other represent. Full error entity from Twilio: $entity"
                ),
                None
              )
            )
        }
      }
  }

}

private object AccountUpdateRequestExecutorImpl {
  private val sidParamKey          = "Sid"
  private val friendlyNameParamKey = "FriendlyName"
  private val statusParamKey       = "Status"
}
