package com.dixa.twilio.client.impl.iam

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.iam.AuthTokenPromoteRequestExecutor
import com.dixa.twilio.client.iam.AuthTokenPromoteRequestExecutor.{AuthTokenPromoteException, AuthTokenPromoteRequest}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.iam.AuthToken

import scala.concurrent.ExecutionContext

private[iam] final class AuthTokenPromoteRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends AuthTokenPromoteRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Accounts

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: AuthTokenPromoteRequest
  ): Either[AuthTokenPromoteException, HttpRequest] =
    createHttpRequestFor(s"/v1/AuthTokens/Promote", connSettings)

  override protected def mapApiException(
      apiException: ApiException
  ): AuthTokenPromoteException.Api =
    AuthTokenPromoteException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): AuthTokenPromoteException.UnspecifiedError =
    AuthTokenPromoteException.UnspecifiedError(msg, cause)

  override protected def parseHttpResponse(
      request: AuthTokenPromoteRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[AuthTokenPromoteException, AuthToken.AuthTokenAndMetaData[AuthToken.Primary]] =
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[AuthTokenPrimaryJsonRep](entity).map(_.toModel)
      case StatusCodes.NotFound => buildResultForNotFoundResponse(entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }

  private def buildResultForNotFoundResponse(
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => createUnspecifiedException(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(AuthTokenPromoteException.SecondaryAuthTokenNotFoundOnAccountException())
          case other =>
            Left(
              createUnspecifiedException(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}
