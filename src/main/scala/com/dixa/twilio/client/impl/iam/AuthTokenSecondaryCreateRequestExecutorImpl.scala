package com.dixa.twilio.client.impl.iam

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.iam.AuthTokenSecondaryCreateRequestExecutor
import com.dixa.twilio.client.iam.AuthTokenSecondaryCreateRequestExecutor.{
  AuthTokenSecondaryCreateException,
  AuthTokenSecondaryCreateRequest
}
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.AuthToken

import scala.concurrent.ExecutionContext

private[iam] final class AuthTokenSecondaryCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AuthTokenSecondaryCreateRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Accounts

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AuthTokenSecondaryCreateRequest
  ): Either[AuthTokenSecondaryCreateException, HttpRequest] =
    createHttpRequestFor(s"/v1/AuthTokens/Secondary", connSettings)

  override protected def mapApiException(
      apiException: ApiException
  ): AuthTokenSecondaryCreateException.Api =
    AuthTokenSecondaryCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AuthTokenSecondaryCreateRequestExecutor.AuthTokenSecondaryCreateException.UnspecifiedError =
    AuthTokenSecondaryCreateException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AuthTokenSecondaryCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AuthTokenSecondaryCreateException, AuthToken.AuthTokenAndMetaData[
    AuthToken.Secondary
  ]] =
    httpResponse.status match {
      case StatusCodes.Created =>
        parseEntityAs[AuthTokenSecondaryJsonRep](entity).map(_.toModel)
      case StatusCodes.NotFound   => buildResultForNotFoundResponse(entity)
      case StatusCodes.BadRequest => buildResultForBadRequestResponse(entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForNotFoundResponse(
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => AuthTokenSecondaryCreateException.UnspecifiedError(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            // Twilio returns this if you do not have the API enabled, and as there is no
            // variables in the path, it should be safe to assume that it's the ony thing
            // this code can mean for this API call.
            Left(AuthTokenSecondaryCreateException.ApiCallNotEnabledOnAccountException())
          case other =>
            Left(
              AuthTokenSecondaryCreateException.UnspecifiedError(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }

  private def buildResultForBadRequestResponse(
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => AuthTokenSecondaryCreateException.UnspecifiedError(e))
      .flatMap { decoded =>
        decoded.code match {
          case 70002L =>
            Left(AuthTokenSecondaryCreateException.SecondaryAuthTokenAlreadyExistsException())
          case other =>
            Left(
              AuthTokenSecondaryCreateException.UnspecifiedError(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
